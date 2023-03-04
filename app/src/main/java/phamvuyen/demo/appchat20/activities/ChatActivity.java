package phamvuyen.demo.appchat20.activities;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import phamvuyen.demo.appchat20.adapters.ChatAdapter;
import phamvuyen.demo.appchat20.databinding.ActivityChatBinding;
import phamvuyen.demo.appchat20.models.ChatMessage;
import phamvuyen.demo.appchat20.models.User;
import phamvuyen.demo.appchat20.utilities.Constants;
import phamvuyen.demo.appchat20.utilities.PreferenceManager;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiveDetails();
        init();
       // listenMessage();

    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodeString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else{
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_SENDER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }

//    private void listenAvailabilityOfReceiver(){
//        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id)
//
//                .addSnapshotListener(ChatActivity.this, ((value, error) -> {
//                    Log.d("value", String.valueOf(value));
//                    if(error != null){
//                        return;
//                    }
//                    if(value != null){
//                        if(value.getLong(Constants.KEY_AVAILABILITY) != null){
//                            int availability = Objects.requireNonNull(
//                                    value.getLong(Constants.KEY_AVAILABILITY)
//                            ).intValue();
//                            isReceiverAvailable = availability == 1;
//                        }
//                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
//                    }
//                    if(isReceiverAvailable){
//                        binding.textAvailability.setVisibility(View.VISIBLE);
//                    }else{
//                        binding.textAvailability.setVisibility(View.GONE);
//                    }
//                }));
//    }

//    private void listenMessage(){
//        database.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
//                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
//                .addSnapshotListener(eventListener);
//        database.collection(Constants.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
//                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
//                .addSnapshotListener(eventListener);
//
//    }
//
//
//    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
//        if(error != null){
//            return;
//        }
//        if(value != null){
//            int count = chatMessages.size();
//            for(DocumentChange documentChange : value.getDocumentChanges()){
//                if(documentChange.getType() == DocumentChange.Type.ADDED){
//                    ChatMessage chatMessage = new ChatMessage();
//                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
//                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
//                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
//                    chatMessage.dateTime = getReadableDataTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
//                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
//                    chatMessages.add(chatMessage);
//                }
//            }
//            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
//            if(count == 0){
//                chatAdapter.notifyDataSetChanged();
//            }else{
//                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
//                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
//            }
//            binding.chatRecyclerView.setVisibility(View.VISIBLE);
//        }
//        binding.progressBas.setVisibility(View.GONE);
//        if(conversationId == null){
//            checkForConversion();
//        }
//    };
    private Bitmap getBitmapFromEncodeString(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    private void loadReceiveDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());

    }

    private String getReadableDataTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotyly(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id);
        }
        checkForConversionRemotyly(receiverUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
    }

    private void checkForConversionRemotyly(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompletionListen);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompletionListen = task ->  {
        if(task.isSuccessful() && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };


//    @Override
//    protected void onResume() {
//        super.onResume();
//        listenAvailabilityOfReceiver();
//    }
}