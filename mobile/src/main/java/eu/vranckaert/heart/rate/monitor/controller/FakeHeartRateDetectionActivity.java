package eu.vranckaert.heart.rate.monitor.controller;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.R;
import eu.vranckaert.heart.rate.monitor.dao.MeasurementDao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Date: 10/06/15
 * Time: 06:16
 *
 * @author Dirk Vranckaert
 */
public class FakeHeartRateDetectionActivity extends Activity {
    private static final int REFRESH_ID = 3;
    private static final int DELETA_ALL_ID = 4;

    private TextView mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fake_heart_rate_detection);

        mContent = (TextView) findViewById(R.id.content);
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, REFRESH_ID, 0, R.string.refresh);
        menu.add(0, DELETA_ALL_ID, 0, R.string.delete_all);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == REFRESH_ID) {
            refreshData();
        } else if (item.getItemId() == DELETA_ALL_ID) {
            new MeasurementDao().deleteAll();
            refreshData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
        mContent.setText("");
        List<Measurement> measurements = new MeasurementDao().findAll();
        Collections.sort(measurements, new Comparator<Measurement>() {
            @Override
            public int compare(Measurement lhs, Measurement rhs) {
                if (lhs.getStartMeasurement() == rhs.getStartMeasurement()) {
                    return 0;
                } else if (lhs.getStartMeasurement() > rhs.getStartMeasurement()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        for (Measurement measurement : measurements) {
            TreeMap<Long,Float> sortedMap = new TreeMap<Long,Float>(new Comparator<Long>() {
                @Override
                public int compare(Long lhs, Long rhs) {
                    return lhs.compareTo(rhs) * -1;
                }
            });
            sortedMap.putAll(measurement.getMeasuredValues());

            String fake = measurement.isFakeHeartRate() ? "==FAKE HEART RATE DETECTED==\n" : "";
            String startTime = "Start: " + new Date(measurement.getStartMeasurement()).toString();
            String firstMeasurement = "First: " + new Date(measurement.getFirstMeasurement()).toString();
            long millisBeforeFirstResult = measurement.getFirstMeasurement() - measurement.getStartMeasurement();
            String timeUntilFirstMeasurementInMillis = "First after: " + millisBeforeFirstResult + " millis";
            String timeUntilFirstMeasurementInSeconds = "First after: " + (millisBeforeFirstResult/1000) + " seconds";
            String averageHeartBeat = "Avg: " + measurement.getAverageHeartBeat() + " BPM";
            String minimumHeartBeat = "Min: " + measurement.getMinimumHeartBeat() + " BPM";
            String maximumHeartBeat = "Max: " + measurement.getMaximumHeartBeat() + " BPM";
            String measurementsText = "";
            for (Entry<Long, Float> entry : sortedMap.entrySet()) {
                if (!TextUtils.isEmpty(measurementsText)) {
                    measurementsText += "\n";
                }
                measurementsText += entry.getValue() + "BPM " + new Date(entry.getKey()).toString();
            }

            String firstItem = "";
            String currentContent =  mContent.getText().toString();
            if (!TextUtils.isEmpty(currentContent)) {
                firstItem = "\n\n";
            }
            mContent.setText(mContent.getText() + fake + firstItem + startTime + "\n" + firstMeasurement + "\n" + timeUntilFirstMeasurementInMillis + "\n" + timeUntilFirstMeasurementInSeconds + "\n" + averageHeartBeat + "\n" + minimumHeartBeat + "\n" + maximumHeartBeat + "\n" + measurementsText);
        }
    }
}
