package com.example.beta1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.3
 * @since 1/2/2023
 * This Activity is the final Activity for the process of making an order.
 * In this Activity, the user selects the most suited hour range that they want to reserve
 * the ParkAd space for.
 */


public class HourSelect extends AppCompatActivity {

    String topHour, bottomHour;
    String parkAdID, userID;
    ParkAd parkAd;
    TextView startTv, endTv;
    String beginHour, beginMinute, endHour, endMinute;
    double finalPrice;

    Spinner SpinBeginHour, SpinBeginMinute, SpinEndHour, SpinEndMinute;
    TimeBarView timeBarView;
    FirebaseDatabase fbDB;
    FirebaseAuth mAuth;
    ArrayList<TimeBarView.Segment> segments = new ArrayList<>();
    Receipt receipt;
    Order order;

    private PaymentsClient paymentsClient;
    private JSONObject transactionInfo = new JSONObject();
    private JSONObject tokenizationSpecification = new JSONObject();
    private JSONObject cardPaymentMethod = new JSONObject();
    private JSONObject merchantInfo = new JSONObject();
    private JSONObject paymentDataRequestJson = new JSONObject();
    PaymentDataRequest paymentDataRequest;
    private final int LOAD_PAYMENT_DATA_REQUEST_CODE = 101;
    private final int SMS_PERMISSION_REQUEST_CODE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hour_select);

        Intent gi = getIntent();
        topHour = gi.getStringExtra("beginHour");
        bottomHour = gi.getStringExtra("endHour");
        parkAdID = gi.getStringExtra("parkAdID");

        startTv = findViewById(R.id.textView6);
        endTv = findViewById(R.id.textView7);
        startTv.setText(topHour);
        endTv.setText(bottomHour);

        fbDB = FirebaseDatabase.getInstance();

        String[] hours = new String[25];
        hours[0] = "Choose hour";
        for (int i = 1; i < 25; i++) {
            if ((i - 1) < 10) hours[i] = "0" + (i - 1);
            else hours[i] = String.valueOf(i - 1);
        }

        String[] minutes = {"Choose minutes", "00", "15", "30", "45"};

        SpinBeginHour = findViewById(R.id.hour1);
        SpinEndHour = findViewById(R.id.hour2);
        SpinBeginMinute = findViewById(R.id.minute1);
        SpinEndMinute = findViewById(R.id.minute2);


        SpinBeginHour.setOnItemSelectedListener(spinListener);
        SpinBeginMinute.setOnItemSelectedListener(spinListener);

        SpinEndHour.setOnItemSelectedListener(spinListener);
        SpinEndMinute.setOnItemSelectedListener(spinListener);

        ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hours);
        SpinBeginHour.setAdapter(hourAdapter);
        SpinEndHour.setAdapter(hourAdapter);

        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minutes);
        SpinBeginMinute.setAdapter(minuteAdapter);
        SpinEndMinute.setAdapter(minuteAdapter);

        readParkAd();
        setTimeBar();


    }

    /**
     * Defines the custom TimeBarView in the Activity.
     * Used to show the user the which hour ranges are available for the ParkAd space and which
     * are not.
     */
    public void setTimeBar() {
        timeBarView = findViewById(R.id.time_bar_view);
        timeBarView.setTopNumber(getValueOfHour(topHour));
        timeBarView.setBottomNumber(getValueOfHour(bottomHour));
        getSegments();

    }

    /**
     * SubMethod for the setTimeBar Method.
     * Used to fill out the TimeBarView with Custom Segments, each describing a time range in which
     * the ParkAd space is already reserved for.
     */
    private void getSegments() {
        DatabaseReference ordersRef = fbDB.getReference("Orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order.getParkAdID().matches(parkAdID)) {
                        String beginHourOrder = order.getBeginHour();
                        String endHourOrder = order.getEndHour();
                        TimeBarView.Segment segment = new TimeBarView.Segment(beginHourOrder, endHourOrder);
                        timeBarView.addSegment(segment);
                        segments.add(segment);
                        timeBarView.invalidate();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * The OnClick Method for the Order Button, which verifies that the time range the user has
     * selected is available, and then creates and Order Object and uploads it to the database.
     * Also launches GooglePay.
     *
     * @param view
     */
    public void makeOrder(View view) {
        if (SpinEndHour.getSelectedItemPosition() == 0 || SpinBeginHour.getSelectedItemPosition() == 0 || SpinBeginMinute.getSelectedItemPosition() == 0 || SpinEndMinute.getSelectedItemPosition() == 0) {
            Services.ErrorAlert("Please enter valid times!", HourSelect.this);
        } else {
            String beginFull = beginHour + ":" + beginMinute;
            String endFull = endHour + ":" + endMinute;
            if (HourInBounds(beginFull, endFull) && !beginFull.matches(endFull)) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
                userID = CurrentUserAuth.getUid();
                String confirmDate = Services.getCurrentTimeFormatted();
                String parkDate = parkAd.getDate();
                double hourlyRate = parkAd.getHourlyRate();
                String sellerID = parkAd.getUserID();
                String parkAddress = parkAd.getAddress();

                order = new Order(parkAdID, userID, confirmDate, parkDate, beginFull, endFull, hourlyRate, sellerID, parkAddress);
                finalPrice = order.getPrice();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Nearly Finished!");
                builder.setMessage("Final Price will be: " + finalPrice);
                builder.setPositiveButton("Pay Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        paymentsClient = createPaymentsClient(HourSelect.this);
                        IsReadyToPayRequest readyToPayRequest = IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString());
                        Task<Boolean> readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest);
                        readyToPayTask.addOnCompleteListener(new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                try {
                                    if (task.getResult(ApiException.class) != null) {
                                        setGooglePayAvailable(task.getResult(ApiException.class));
                                    }
                                } catch (ApiException exception) {
                                    // Error determining readiness to use Google Pay.
                                    // Inspect the logs for more details.
                                }
                            }
                        });
                    }
                });
                builder.show();

            } else {
                Services.ErrorAlert("Select Hour in Available Range", HourSelect.this);
            }
        }
    }


    /**
     * SubMethod for the MakeOrder Method.
     * Used to read the information of the current ParkAd and copy parameters to the Order Object.
     * Also updates the TimeBarView with a Segment if the current date and the ParkAd date match.
     * (In order to make sure orders can't pass for times that have already passed).
     */
    public void readParkAd() {
        DatabaseReference parkAdref = fbDB.getReference("ParkAds").child(parkAdID);
        parkAdref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkAd = snapshot.getValue(ParkAd.class);
                SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());
                currentDate = Services.addLeadingZerosToDate(currentDate, false);
                String parkAdDateStr = parkAd.getDate();
                parkAdDateStr = Services.addLeadingZerosToDate(parkAdDateStr, false);

                if (parkAdDateStr.matches(currentDate)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    Date current2 = new Date(currentTimeMillis);
                    String currentHour = hourFormat.format(current2);
                    if (Services.isHourBetween(currentHour, parkAd.getBeginHour(), parkAd.getFinishHour())) {
                        TimeBarView.Segment segment = new TimeBarView.Segment(parkAd.getBeginHour(), Services.roundToNextQuarterHour(currentHour));
                        segments.add(segment);
                        timeBarView.addSegment(segment);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * ItemSelectedListener for the hour/minute Spinners.
     * Used to update the time String params for the Order.
     */
    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView == SpinBeginHour) {
                beginHour = adapterView.getItemAtPosition(i).toString();
            } else if (adapterView == SpinEndHour) {
                endHour = adapterView.getItemAtPosition(i).toString();
            } else if (adapterView == SpinBeginMinute) {
                beginMinute = adapterView.getItemAtPosition(i).toString();
            } else {
                endMinute = adapterView.getItemAtPosition(i).toString();
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    /**
     * Boolean SubMethod for the time verification process of the MakeOrder Method.
     *
     * @param hour1: Time String
     * @param hour2: Time String
     * @return: The Method returns true if the following conditions are met:
     * 1.The time described in the hour1 String is before the time described in the hour2 String.
     * 2.The time described in the hour1 String is between the The time described in the top and
     * bottom hour String of the TimeBarView.
     * 3.The time described in the hour3 String is between the The time described in the top and
     * bottom hour String of the TimeBarView.
     */
    private boolean HourInBounds(String hour1, String hour2) {
        if (!Services.isHourBetween(hour1, topHour, bottomHour)) {
            if (!(hour1.matches(topHour))) {
                return false;
            }
        }
        if (getValueOfHour(hour2) <= getValueOfHour(hour1)) return false;

        if (!Services.isHourBetween(hour2, topHour, bottomHour)) {
            if (!(hour2.matches(bottomHour))) {
                return false;
            }
        }

        for (TimeBarView.Segment segment : segments) {
            if (checkOverlap(hour1, hour2, segment.getBeginHour(), segment.getEndHour()))
                return false;
        }
        return true;
    }


    /**
     * Boolean SubMethod for the time verification process of the HourInBounds Method.
     *
     * @param beginHour: Time String
     * @param endHour:   Time String
     * @param limit1:    Time String
     * @param limit2:    Time String
     * @return: The Method returns true if the time range between the time range described in the
     * beginHour and the endHour Strings overlaps with the time range between the time range
     * described in the limit1 and limit2 Strings.
     */
    public static boolean checkOverlap(String beginHour, String endHour, String limit1, String limit2) {
        double begin = getValueOfHour(beginHour);
        double end = getValueOfHour(endHour);
        double limitOne = getValueOfHour(limit1);
        double limitTwo = getValueOfHour(limit2);

        if (begin > end) {
            return false;
        }
        if (limitOne > limitTwo) {
            double temp = limitOne;
            limitOne = limitTwo;
            limitTwo = temp;
        }
        return (begin >= limitOne && begin < limitTwo) || (end > limitOne && end <= limitTwo) || (begin <= limitOne && end >= limitTwo);
    }

    /**
     * Double SubMethod for the time verification process of the CheckOverlap Method.
     *
     * @param time: Time String
     * @return the double value of the given time String (e.g: the input "16:45" will return 1645).
     */
    private static double getValueOfHour(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour + minute / 60.0;
    }


    /**
     * This Method creates a PaymentsClient to use for Google Pay.
     *
     * @param context: The App's Context.
     * @return: The Method returns the PaymentsClient Object.
     */
    private PaymentsClient createPaymentsClient(Context context) {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        return Wallet.getPaymentsClient(context, walletOptions);
    }

    private JSONObject baseCardPaymentMethod = new JSONObject();

    {
        try {
            baseCardPaymentMethod.put("type", "CARD");
            JSONObject parameters = new JSONObject();
            parameters.put("allowedCardNetworks", new JSONArray(Arrays.asList("VISA", "MASTERCARD")));
            parameters.put("allowedAuthMethods", new JSONArray(Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS")));

            baseCardPaymentMethod.put("parameters", parameters);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject googlePayBaseConfiguration = new JSONObject();

    {
        try {
            googlePayBaseConfiguration.put("apiVersion", 2);
            googlePayBaseConfiguration.put("apiVersionMinor", 0);

            JSONArray allowedPaymentMethods = new JSONArray();
            allowedPaymentMethods.put(baseCardPaymentMethod);

            googlePayBaseConfiguration.put("allowedPaymentMethods", allowedPaymentMethods);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * SubMethod for the Google Pay payment process that checks if Google Pay is available to
     * continue the process.
     *
     * @param available: Boolean - if true, then Google Pay is available, if false, the Google Pay
     *                   isn't available.
     */
    private void setGooglePayAvailable(Boolean available) {
        if (available) {
            requestPayment();
        } else {
            Services.ErrorAlert("Error launching Google Pay!", HourSelect.this);
        }
    }

    /**
     * This Method sets up all of the necessary parameters for a Google Pay payment, and then
     * requests a payment.
     */
    private void requestPayment() {
        try {
            tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
            JSONObject parameters = new JSONObject();
            parameters.put("gateway", "example");
            parameters.put("gatewayMerchantId", "exampleGatewayMerchantId");
            tokenizationSpecification.put("parameters", parameters);
            try {
                transactionInfo.put("totalPrice", String.valueOf(finalPrice));
                transactionInfo.put("totalPriceStatus", "FINAL");
                transactionInfo.put("currencyCode", "NIS");
                try {
                    cardPaymentMethod.put("type", "CARD");
                    cardPaymentMethod.put("tokenizationSpecification", tokenizationSpecification);
                    JSONObject parameters2 = new JSONObject();
                    parameters2.put("allowedCardNetworks", new JSONArray(Arrays.asList("VISA", "MASTERCARD")));
                    parameters2.put("allowedAuthMethods", new JSONArray(Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS")));
                    parameters2.put("billingAddressRequired", true);
                    JSONObject billingAddressParameters = new JSONObject();
                    billingAddressParameters.put("format", "FULL");
                    parameters2.put("billingAddressParameters", billingAddressParameters);
                    cardPaymentMethod.put("parameters", parameters2);
                    try {
                        merchantInfo.put("merchantName", "Example Merchant");
                        merchantInfo.put("merchantId", "01234567890123456789");

                        try {
                            paymentDataRequestJson = new JSONObject(googlePayBaseConfiguration.toString());
                            paymentDataRequestJson.put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod));
                            paymentDataRequestJson.put("transactionInfo", transactionInfo);
                            paymentDataRequestJson.put("merchantInfo", merchantInfo);

                            try {
                                paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
                                AutoResolveHelper.resolveTask(
                                        paymentsClient.loadPaymentData(paymentDataRequest),
                                        this,
                                        LOAD_PAYMENT_DATA_REQUEST_CODE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * OnActivityResult for the RequestPayment Method. Used to check if the Payment went through
     * as planned.
     *
     * @param requestCode: The Payment Data request code. (Integer)
     * @param resultCode:  The result code from the payment attempt. (Integer)
     * @param data:        Intent containing the payment data.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    if (paymentData != null) {
                        handlePaymentSuccess(paymentData);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    // The user cancelled without selecting a payment method.
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    if (status != null) {
                        Services.ErrorAlert("An error occurred during the payment process: " + status.getStatusMessage(), HourSelect.this);
                    }
                    break;
                default:
                    // Unexpected resultCode.
                    break;
            }
        }
    }

    /**
     * Final Method for the Google Pay payment process. Used to notify the user and the seller
     * that the payment went through.
     *
     * @param paymentData
     */
    private void handlePaymentSuccess(PaymentData paymentData) {
        String confirmDate = order.getConfirmDate();
        String sellerID = order.getSellerId();
        DatabaseReference ordersRef = fbDB.getReference("Orders");
        String Orderkey = ordersRef.push().getKey();
        ordersRef.child(Orderkey).setValue(order);
        DatabaseReference usersRef = fbDB.getReference("Users");
        usersRef.child(userID).child("Orders").child(Orderkey).setValue(order);
        UUID uuid = UUID.randomUUID();
        String paymentID = uuid.toString();

        DatabaseReference sellerRef = fbDB.getReference("Users").child(sellerID);
        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User seller = snapshot.getValue(User.class);
                receipt = new Receipt(sellerID, userID, parkAdID, Orderkey, finalPrice, confirmDate, paymentID, seller.getName());
                DatabaseReference receiptRefSeller = fbDB.getReference("Users").child(sellerID).child("Receipts");
                String receiptKey = receiptRefSeller.push().getKey();
                receiptRefSeller.child(receiptKey).setValue(receipt);
                DatabaseReference receiptRefUser = fbDB.getReference("Users").child(userID).child("Receipts");
                receiptRefUser.child(receiptKey).setValue(receipt);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
                    } else {
                        SmsManager smsManager = SmsManager.getDefault();
                        String formattedNumber = "+972" + seller.getPhoneNumber().substring(1);
                        smsManager.sendTextMessage(formattedNumber, null, "I ordered from your ParkAd using Spark!", null, null);
                    }
                } else {
                    SmsManager smsManager = SmsManager.getDefault();
                    String formattedNumber = "+972" + seller.getPhoneNumber().substring(1);
                    smsManager.sendTextMessage(formattedNumber, null, "I ordered from your ParkAd using Spark!", null, null);
                }


                AlertDialog.Builder adb = new AlertDialog.Builder(HourSelect.this);
                adb.setTitle("Payment Success!");
                adb.setMessage("You may now return to the Home Screen.");
                adb.setNeutralButton("Return", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent si = new Intent(HourSelect.this, Navi.class);
                        startActivity(si);
                    }
                });
                AlertDialog dialog = adb.create();
                dialog.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


}