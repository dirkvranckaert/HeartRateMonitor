package eu.vranckaert.heart.rate.monitor.controller;

import android.util.Log;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 22/02/16
 * Time: 16:11
 *
 * @author Dirk Vranckaert
 */
public class HeartRateObserver {
    private static final List<WeakReference<HeartRateObservable>> observables = new ArrayList<>();

    private static void cleanupObservables() {
        List<WeakReference<HeartRateObservable>> itemsForRemoval = new ArrayList<>();

        int size = observables.size();
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            HeartRateObservable observable = observableWeakReference.get();
            if (observable == null) {
                itemsForRemoval.add(observableWeakReference);
            }
        }

        observables.removeAll(itemsForRemoval);
    }

    public static void register(HeartRateObservable observable) {
        observables.add(new WeakReference<>(observable));
        cleanupObservables();
    }

    public static void unregister(HeartRateObservable observable) {
        cleanupObservables();
        int size = observables.size();
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            HeartRateObservable thisObservable = observableWeakReference.get();
            if (thisObservable != null && observable.equals(thisObservable)) {
                observables.remove(observableWeakReference);
                break;
            }
        }
    }

    public static void onMeasurementsSynced() {
        Log.d("dirk", "Cleanup observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Measurements synced, notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            final HeartRateObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                HeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("dirk", "Executing observable on UI thread");
                        observable.onMeasurementsSynced();
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public interface HeartRateObservable {
        void onMeasurementsSynced();
    }
}
