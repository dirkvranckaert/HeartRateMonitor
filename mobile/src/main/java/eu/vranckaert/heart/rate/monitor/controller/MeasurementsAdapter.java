package eu.vranckaert.heart.rate.monitor.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.view.MeasurementListItemView;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 02/09/15
 * Time: 16:40
 *
 * @author Dirk Vranckaert
 */
public class MeasurementsAdapter extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;
    private List<Measurement> mMeasurements = new ArrayList<>();

    public MeasurementsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setMeasurements(List<Measurement> measurements) {
        mMeasurements.clear();
        mMeasurements.addAll(measurements);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMeasurements.size();
    }

    @Override
    public Measurement getItem(int position) {
        return mMeasurements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MeasurementListItemView view = MeasurementListItemView.fromView(convertView);

        if (view == null) {
            view = new MeasurementListItemView(mLayoutInflater, parent);
        }
        view.setMeasurement(getItem(position));

        return view.getView();
    }
}
