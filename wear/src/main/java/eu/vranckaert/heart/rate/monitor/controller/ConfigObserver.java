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
public class ConfigObserver {
    private static final List<WeakReference<ConfigObservable>> observables = new ArrayList<>();

    private static void cleanupObservables() {
        List<WeakReference<ConfigObservable>> itemsForRemoval = new ArrayList<>();

        int size = observables.size();
        for (int i = 0; i < size; i++) {
            WeakReference<ConfigObservable> observableWeakReference = observables.get(i);
            ConfigObservable observable = observableWeakReference.get();
            if (observable == null) {
                itemsForRemoval.add(observableWeakReference);
            }
        }

        observables.removeAll(itemsForRemoval);
    }

    public static void register(ConfigObservable observable) {
        observables.add(new WeakReference<>(observable));
        cleanupObservables();
    }

    public static void unregister(ConfigObservable observable) {
        cleanupObservables();
        int size = observables.size();
        for (int i = 0; i < size; i++) {
            WeakReference<ConfigObservable> observableWeakReference = observables.get(i);
            ConfigObservable thisObservable = observableWeakReference.get();
            if (thisObservable != null && observable.equals(thisObservable)) {
                observables.remove(observableWeakReference);
                break;
            }
        }
    }

    public static void onPhoneSetupCompletionChanged() {
        Log.d("dirk", "Executing observer on all observables");
        cleanupObservables();

        int size = observables.size();
        Log.d("dirk", "Notifying " + size + " observable(s)");
        for (int i = 0; i < size; i++) {
            WeakReference<ConfigObservable> observableWeakReference = observables.get(i);
            final ConfigObservable observable = observableWeakReference.get();
            if (observable != null) {
                Log.d("dirk", "Observable on position " + i + " is valid");
                WearHeartRateApplication.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        observable.onPhoneSetupCompletionChanged();
                    }
                });
            } else {
                Log.d("dirk", "Observable on position " + i + " is INvalid");
            }
        }
    }

    public interface ConfigObservable {
        void onPhoneSetupCompletionChanged();
    }
}
