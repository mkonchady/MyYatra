package org.mkonchady.myyatra;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


public class ResultInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.results_info);

        Intent intent = getIntent();
        float yourDistance = intent.getFloatExtra("yours", 0.0f);
        float computerDistance = intent.getFloatExtra("computer", 0.0f);
        float difference = yourDistance - computerDistance;

        // set the data
        final TextView resYourDistance = (TextView) findViewById(R.id.resTextView01b);
        String out = Float.toString(yourDistance) + " " + getResources().getText(R.string.kms);
        resYourDistance.setText(out);
        final TextView resComputerDistance = (TextView) findViewById(R.id.resTextView02b);
        out = Float.toString(computerDistance) + " " +  getResources().getText(R.string.kms);
        resComputerDistance.setText(out);
        final TextView resDifferenceDistance = (TextView) findViewById(R.id.resTextView03b);
        out = Float.toString(difference) + " " + getResources().getText(R.string.kms);
        resDifferenceDistance.setText(out);

        final TextView resScore = (TextView) findViewById(R.id.resTextView04b);
        int score = 0;
        if (difference < 0) {
            Toast.makeText(this, "Wow !! You beat the machine...", Toast.LENGTH_LONG).show();
            score = 11;

        } else  if (difference == 0) {
            score = 10;
        } else {
            float diff = difference / computerDistance;
            if (diff < 0.1) score = 9;
            else if (diff < 0.2) score = 8;
            else score = 7;
        }
        out = score + " out of 10";
        resScore.setText(out);
    }

}
