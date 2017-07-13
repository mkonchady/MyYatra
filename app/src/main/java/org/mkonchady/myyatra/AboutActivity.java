package org.mkonchady.myyatra;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AboutActivity extends Activity {

    LayoutInflater inflater = null;
    TableLayout tableLayout = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        inflater = getLayoutInflater();
        context = this;
        setContentView(R.layout.activity_about);
        tableLayout = (TableLayout) findViewById(R.id.tableAboutlayout);
        addRow("Tour", "A <b> <u> round trip </u> </b> journey visiting a city just once.", false);
        addRow("Shortest Tour", "The <b> <u> least </u> </b> round trip distance of a tour ", true);
        addRow("Number of cities", "The number of cities increases the number of tours by a factorial function. A tour of <b> <u>16 </u> </b>cities has over a  <b> <u>trillion</u> </b> tours.", false);
        addRow("Algorithm", "This app uses a <b> <u> Genetic Algorithm </u> </b> to find a near optimal tour", true);
        addRow("Solution Time", "Given more time, the algorithm will try a wider range of possibilities and should find the shortest tour", false);
    }


    private void addRow(String label, String description, boolean background) {
        TableRow tr;
        TextView labelView;
        TextView descriptionView;
        if (background) {
            tr = (TableRow) inflater.inflate(R.layout.table_about_color_row, tableLayout, false);
            labelView = (TextView) tr.findViewById(R.id.aboutColorLabel);
            descriptionView = (TextView) tr.findViewById(R.id.aboutColorDescription);
        } else {
            tr = (TableRow) inflater.inflate(R.layout.table_about_plain_row, tableLayout, false);
            labelView = (TextView) tr.findViewById(R.id.aboutPlainLabel);
            descriptionView = (TextView) tr.findViewById(R.id.aboutPlainDescription);
        }
        labelView.setText(label);
        descriptionView.setText(Html.fromHtml(description));
        tableLayout.addView(tr);        // add to the table

        View v = new View(this);
        v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(ContextCompat.getColor(context, R.color.Black));
        tableLayout.addView(v);

    }
}