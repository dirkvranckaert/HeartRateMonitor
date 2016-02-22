package eu.vranckaert.heart.rate.monitor.controller;

import android.util.Log;
import eu.vranckaert.heart.rate.monitor.WearHeartRateApplication;

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

    public static void onHeartBeatMeasured(final float bpm) {
        Log.d("dirk", "Executing observer on all observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            final HeartRateObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                WearHeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        observable.onHeartBeatMeasured(bpm);
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public static void onStartMeasuringHeartBeat() {
        Log.d("dirk", "Executing observer on all observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            final HeartRateObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                WearHeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        observable.onStartMeasuringHeartBeat();
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public static void onStopMeasuringHeartBeat() {
        Log.d("dirk", "Executing observer on all observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            final HeartRateObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                WearHeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        observable.onStopMeasuringHeartBeat();
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public static void onHeartRateMeasurmentsSentToPhone() {
        Log.d("dirk", "Executing observer on all observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            final HeartRateObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                WearHeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        observable.onHeartRateMeasurementsSentToPhone();
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public static void onMeasurementsAckReceived() {
        Log.d("dirk", "Executing observer on all observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<HeartRateObservable> observableWeakReference = observables.get(i);
            final HeartRateObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                WearHeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        observable.onMeasurementsAckReceived();
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public interface HeartRateObservable {
        void onHeartBeatMeasured(float bpm);

        void onStartMeasuringHeartBeat();

        void onStopMeasuringHeartBeat();

        void onHeartRateMeasurementsSentToPhone();

        void onMeasurementsAckReceived();
    }
}
