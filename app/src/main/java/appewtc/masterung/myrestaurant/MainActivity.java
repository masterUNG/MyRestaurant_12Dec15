package appewtc.masterung.myrestaurant;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    //Explicit
    private ManageTABLE objManageTABLE;
    private EditText userEditText, passwordEditText;
    private String userString, passwordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind Widget
        bindWidget();

        //Connected Database
        objManageTABLE = new ManageTABLE(this);

        //Tester Add Value
        //testerAddValue();

        //Delete All SQLite
        deleteAllSQLite();

        //Synchronize JSON to SQLite
        synJSONtoSQLite();


    }   // Main Method

    private void bindWidget() {
        userEditText = (EditText) findViewById(R.id.editText);
        passwordEditText = (EditText) findViewById(R.id.editText2);
    }

    public void clickLogin(View view) {

        userString = userEditText.getText().toString().trim();
        passwordString = passwordEditText.getText().toString().trim();

        //Check Space
        if (userString.equals("") || passwordString.equals("")) {

            //Have Space
            MyAlertDialog objMyAlertDialog = new MyAlertDialog();
            objMyAlertDialog.errorDialog(MainActivity.this, "Have Space", "Please Fill All Every Blank");

        } else {

            //No Space
            checkUser();

        }

    }   // clickLogin

    private void checkUser() {

        try {

            String[] strMyResult = objManageTABLE.searchUser(userString);
            // Check Password
            if (passwordString.equals(strMyResult[2])) {

                //Intent to OrderListView
                Intent objIntent = new Intent(MainActivity.this, OrderListView.class);
                objIntent.putExtra("Officer", strMyResult[3]);
                startActivity(objIntent);


            } else {

                MyAlertDialog objMyAlertDialog = new MyAlertDialog();
                objMyAlertDialog.errorDialog(MainActivity.this, "Password False", "Please Try again Password False");

            }

        } catch (Exception e) {
            MyAlertDialog objMyAlertDialog = new MyAlertDialog();
            objMyAlertDialog.errorDialog(MainActivity.this, "User False", "No " + userString + " in my Database");
        }

    }


    private void synJSONtoSQLite() {

        //Setup my Policy
        StrictMode.ThreadPolicy myThreadPolicy = new StrictMode.ThreadPolicy
                .Builder().permitAll().build();
        StrictMode.setThreadPolicy(myThreadPolicy);

        int intTable = 1;
        while (intTable <= 2) {

            InputStream objInputStream = null;
            String strJSON = null;
            String strURLuser = "http://swiftcodingthai.com/12dec/php_get_data_master.php";
            String strURLfood = "http://swiftcodingthai.com/12dec/php_get_data_food.php";
            HttpPost objHttpPost = null;

            //1. Create InputStream
            try {

                HttpClient objHttpClient = new DefaultHttpClient();

                switch (intTable) {
                    case 1:
                        objHttpPost = new HttpPost(strURLuser);
                        break;
                    case 2:
                        objHttpPost = new HttpPost(strURLfood);
                        break;
                }

                HttpResponse objHttpResponse = objHttpClient.execute(objHttpPost);
                HttpEntity objHttpEntity = objHttpResponse.getEntity();
                objInputStream = objHttpEntity.getContent();

            } catch (Exception e) {
                Log.d("Rest", "InputStream ==> " + e.toString());
            }

            //2. Create strJSON
            try {

                BufferedReader objBufferedReader = new BufferedReader(new InputStreamReader(objInputStream, "UTF-8"));
                StringBuilder objStringBuilder = new StringBuilder();
                String strLine = null;

                while ((strLine = objBufferedReader.readLine()) != null) {
                    objStringBuilder.append(strLine);
                }   // while

                objInputStream.close();
                strJSON = objStringBuilder.toString();


            } catch (Exception e) {
                Log.d("Rest", "strJSON ==> " + e.toString());
            }

            //3. Update to SQLite
            try {

                JSONArray objJsonArray = new JSONArray(strJSON);
                for (int i = 0; i < objJsonArray.length(); i++) {

                    JSONObject object = objJsonArray.getJSONObject(i);

                    switch (intTable) {
                        case 1:

                            //For userTABLE
                            String strUser = object.getString("User");
                            String strPassword = object.getString("Password");
                            String strName = object.getString("Name");
                            objManageTABLE.addValueToUser(strUser, strPassword, strName);

                            break;
                        case 2:

                            //For foodTABLE
                            String strFood = object.getString("Food");
                            String strSource = object.getString("Source");
                            String strPrice = object.getString("Price");
                            objManageTABLE.addValueToFood(strFood, strSource, strPrice);

                            break;
                    }   // switch

                }   // for

            } catch (Exception e) {
                Log.d("Rest", "Update ==> " + e.toString());
            }


            intTable += 1;
        }   // while


    }   // synJSONtoSQLie

    private void deleteAllSQLite() {

        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase("Restaurant.db", MODE_PRIVATE, null);
        objSqLiteDatabase.delete("userTABLE", null, null);
        objSqLiteDatabase.delete("foodTABLE", null, null);

    }

    private void testerAddValue() {

        objManageTABLE.addValueToUser("user", "pass", "มาสเตอร์ อึ่ง");
        objManageTABLE.addValueToFood("food", "source", "price");

    }

}   // Main Class
