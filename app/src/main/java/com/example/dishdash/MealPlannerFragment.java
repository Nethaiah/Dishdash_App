package com.example.dishdash;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dishdash.Adapters.GenerateMealPlanAdapter;
import com.example.dishdash.Adapters.GenerateMealPlanNutritionAdapter;
import com.example.dishdash.GenerateDailyMealPlannerClass.GenerateMealAPIResponse;
import com.example.dishdash.GenerateDailyMealPlannerClass.Meal;
import com.example.dishdash.GenerateDailyMealPlannerClass.Nutrients;
import com.example.dishdash.Listeners.GenerateMealPlanResponseListener;
import com.example.dishdash.Listeners.RecipeClickResponseListener;
import com.example.dishdash.RequestManagers.GenerateMealPlanRequestManager;
import com.example.dishdash.databinding.FragmentMealPlannerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MealPlannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MealPlannerFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MealPlannerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MealPlannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MealPlannerFragment newInstance(String param1, String param2) {
        MealPlannerFragment fragment = new MealPlannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    FragmentMealPlannerBinding binding;
    GenerateMealPlanAdapter generateMealPlanAdapter;
    GenerateMealPlanNutritionAdapter generateMealPlanNutritionAdapter;
    GenerateMealPlanRequestManager generateMealPlanRequestManager;
    View generateMealPlanDialogView, overwriteMealPlanDialogView;
    AutoCompleteTextView mealPlannerDietaryPreference;
    EditText mealPlannerExclusions, mealPlannerTotalCalories;
    ArrayAdapter<String> adapter;
    String[] dietaryPreferenceItem = {"Gluten Free", "Ketogenic", "Vegetarian", "Lacto-Vegetarian", "Ovo-Vegetarian", "Vegan", "Pescetarian", "Paleo", "Primal", "Low FODMAP", "Whole30"};
    Button overwriteMealPlanOkay, overwriteMealPlanNo, okay, cancel, mealPlannerDatePicker, mealPlannerBreakfastTimeTimePicker, mealPlannerLunchTimeTimePicker, mealPlannerDinnerTimeTimePicker;
    TextView mealPlannerBreakfastTime, mealPlannerLunchTime, mealPlannerDinnerTime, mealPlannerStartDate;
    String dietaryPreference, breakfastTimeString, lunchTimeString, dinnerTimeString;
    Calendar selectedDate;
    HashSet<String> selectedTimes;
    AlertDialog generateMealPlanDialog, overwriteMealPlanDialog;

    List<Meal> mealList;
    Nutrients nutrients;
    String[] mealTimes;
    String startDateString;

    DatabaseReference mealReference;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Inflate the layout for this fragment
        selectedTimes = new HashSet<>();
        binding = FragmentMealPlannerBinding.inflate(inflater, container, false);
        InsetsUtil.applyInsets(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        fetchMealPlan();

        binding.mealPlannerRecipeRecyclerView.setHasFixedSize(true);
        binding.mealPlannerRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.mealPlannerRecipeNutritionRecyclerView.setHasFixedSize(true);
        binding.mealPlannerRecipeNutritionRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        generateMealPlanRequestManager = new GenerateMealPlanRequestManager(getContext());

        binding.generateMealPlanner.setOnClickListener(v -> {
            selectedTimes.clear();

            generateMealPlanDialogView = getLayoutInflater().inflate(R.layout.generate_daily_meal_plan_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(generateMealPlanDialogView);
            generateMealPlanDialog = builder.create();

            generateMealPlanDialog.setOnDismissListener(dialog -> {
                selectedTimes.clear();
                Log.d("GenerateMealPlan", "Dialog dismissed, selected times cleared.");
            });

            generateMealPlanDialog.show();

            mealPlannerDietaryPreference = generateMealPlanDialogView.findViewById(R.id.mealPlannerDietaryPreference);
            adapter = new ArrayAdapter<>(requireContext(), R.layout.dietary_preference_list_view, dietaryPreferenceItem);
            mealPlannerDietaryPreference.setAdapter(adapter);

            mealPlannerTotalCalories = generateMealPlanDialogView.findViewById(R.id.mealPlannerTotalCalories);
            mealPlannerExclusions = generateMealPlanDialogView.findViewById(R.id.mealPlannerExclusions);

            mealPlannerDietaryPreference.setOnItemClickListener((parent, view1, position, id) -> {
                dietaryPreference = parent.getItemAtPosition(position).toString();
            });

            okay = generateMealPlanDialogView.findViewById(R.id.okay);
            cancel = generateMealPlanDialogView.findViewById(R.id.cancel);

            cancel.setOnClickListener(vbuttonCancel -> {
                generateMealPlanDialog.dismiss();
            });

            okay.setOnClickListener(vbuttonOkay -> {
                String calories = mealPlannerTotalCalories.getText().toString();
                String exclusions = mealPlannerExclusions.getText().toString();

                // Get times from the dialog's TextViews
                breakfastTimeString = mealPlannerBreakfastTime.getText().toString();
                lunchTimeString = mealPlannerLunchTime.getText().toString();
                dinnerTimeString = mealPlannerDinnerTime.getText().toString();

                // Check if required fields are empty
                if (selectedDate == null) {
                    Toast.makeText(getContext(), "Please select a start date.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (calories.isEmpty() || Integer.parseInt(calories) < 1200 || Integer.parseInt(calories) > 4500) {
                    Toast.makeText(getContext(), "Please enter a valid caloric goal between 1200 and 4500.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (breakfastTimeString.isEmpty() || lunchTimeString.isEmpty() || dinnerTimeString.isEmpty()) {
                    Toast.makeText(getContext(), "Please set times for all meals.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db = FirebaseDatabase.getInstance();
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();

                mealReference = db.getReference("Users").child(user.getUid()).child("mealPlan");

                mealReference.orderByChild("startDate").equalTo(mealPlannerStartDate.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            overwriteMealPlanDialogView = getLayoutInflater().inflate(R.layout.overwrite_existing_meal_plan_dialog, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setView(overwriteMealPlanDialogView);
                            overwriteMealPlanDialog = builder.create();
                            overwriteMealPlanDialog.show();

                            overwriteMealPlanNo = overwriteMealPlanDialogView.findViewById(R.id.no);
                            overwriteMealPlanOkay = overwriteMealPlanDialogView.findViewById(R.id.yes);

                            overwriteMealPlanNo.setOnClickListener(no -> {
                                if (overwriteMealPlanDialog.isShowing()) {
                                    overwriteMealPlanDialog.dismiss();
                                }

                                if (generateMealPlanDialog.isShowing()) {
                                    generateMealPlanDialog.dismiss();
                                }
                            });

                            overwriteMealPlanOkay.setOnClickListener(yes -> {
                                deleteExistingMealPlan(snapshot);
                                generateMealPlan(calories, exclusions, breakfastTimeString, lunchTimeString, dinnerTimeString);

                                if (overwriteMealPlanDialog.isShowing()) {
                                    overwriteMealPlanDialog.dismiss();
                                }

                                if (generateMealPlanDialog.isShowing()) {
                                    generateMealPlanDialog.dismiss();
                                }
                            });
                        } else {
                            generateMealPlan(calories, exclusions, breakfastTimeString, lunchTimeString, dinnerTimeString);

                            if (generateMealPlanDialog.isShowing()) {
                                generateMealPlanDialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to check for existing meal plan.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            mealPlannerDatePicker = generateMealPlanDialogView.findViewById(R.id.mealPlannerDatePicker);
            mealPlannerStartDate = generateMealPlanDialogView.findViewById(R.id.mealPlannerStartDate);

            mealPlannerDatePicker.setOnClickListener(v1 -> showDatePicker());
            mealPlannerBreakfastTimeTimePicker = generateMealPlanDialogView.findViewById(R.id.mealPlannerBreakfastTimeTimePicker);
            mealPlannerLunchTimeTimePicker = generateMealPlanDialogView.findViewById(R.id.mealPlannerLunchTimeTimePicker);
            mealPlannerDinnerTimeTimePicker = generateMealPlanDialogView.findViewById(R.id.mealPlannerDinnerTimeTimePicker);

            mealPlannerBreakfastTime = generateMealPlanDialogView.findViewById(R.id.mealPlannerBreakfastTime);
            mealPlannerLunchTime = generateMealPlanDialogView.findViewById(R.id.mealPlannerLuchTime);
            mealPlannerDinnerTime = generateMealPlanDialogView.findViewById(R.id.mealPlannerDinnerTime);

            mealPlannerBreakfastTimeTimePicker.setOnClickListener(v1 -> showTimePicker(mealPlannerBreakfastTime));
            mealPlannerLunchTimeTimePicker.setOnClickListener(v1 -> showTimePicker(mealPlannerLunchTime));
            mealPlannerDinnerTimeTimePicker.setOnClickListener(v1 -> showTimePicker(mealPlannerDinnerTime));
        });
    }

    private void generateMealPlan(String calories, String exclusions, String breakfastTime, String lunchTime, String dinnerTime){
        generateMealPlanRequestManager.getGenerateMealPlan(new GenerateMealPlanResponseListener() {
            @Override
            public void didFetchGenerateMealPlan(GenerateMealAPIResponse response, String message) {
                if (response != null && response.meals != null && response.nutrients != null) {
                    Map<String, Object> mealPlanData = new HashMap<>();
                    mealPlanData.put("startDate", mealPlannerStartDate.getText().toString());
                    mealPlanData.put("mealTimes", Map.of("breakfast", breakfastTime, "lunch", lunchTime, "dinner", dinnerTime));
                    mealPlanData.put("meals", response.meals);
                    mealPlanData.put("nutrients", response.nutrients);

                    mealReference.push().setValue(mealPlanData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Meal plan stored successfully!", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getContext(), "Failed to store meal plan.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void didError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }, calories, dietaryPreference, exclusions);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            if (selected.before(Calendar.getInstance())) {
                Toast.makeText(requireContext(), "You cannot select a past date.", Toast.LENGTH_SHORT).show();
            } else {
                Calendar tenDaysFromNow = Calendar.getInstance();
                tenDaysFromNow.add(Calendar.DAY_OF_YEAR, 10);
                if (selected.after(tenDaysFromNow)) {
                    Toast.makeText(requireContext(), "You cannot select a date more than 10 days in the future.", Toast.LENGTH_SHORT).show();
                } else {
                    selectedDate = selected;
                    mealPlannerStartDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showTimePicker(TextView timeTextView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            String period = (hourOfDay < 12) ? "AM" : "PM";
            @SuppressLint("DefaultLocale") String formattedTime = String.format("%02d:%02d %s", hourOfDay % 12 == 0 ? 12 : hourOfDay % 12, minute, period);

            // Temporarily remove the current time from selectedTimes if it's already set for this TextView
            String currentTextViewTime = timeTextView.getText().toString();
            if (selectedTimes.contains(currentTextViewTime)) {
                selectedTimes.remove(currentTextViewTime);
            }

            // Convert formatted time to minutes since midnight
            int selectedTimeInMinutes = convertTimeToMinutes(hourOfDay, minute);

            boolean isValid = true;
            for (String existingTime : selectedTimes) {
                // Get the existing time in minutes
                String[] timeParts = existingTime.split(":| ");
                int hour = Integer.parseInt(timeParts[0]);
                int min = Integer.parseInt(timeParts[1]);
                int existingTimeInMinutes = convertTimeToMinutes(hour + (timeParts[2].equals("PM") && hour != 12 ? 12 : 0), min);

                // Check the difference between selected time and existing time
                if (Math.abs(selectedTimeInMinutes - existingTimeInMinutes) < 180) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                // Add the new time to selectedTimes and update the TextView
                selectedTimes.add(formattedTime);
                timeTextView.setText(formattedTime);
            } else {
                // Re-add the current time to selectedTimes if the new time is invalid
                if (!currentTextViewTime.isEmpty()) {
                    selectedTimes.add(currentTextViewTime);
                }
                Toast.makeText(requireContext(), "The selected time must be at least 3 hours apart from other times.", Toast.LENGTH_SHORT).show();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    private int convertTimeToMinutes(int hourOfDay, int minute) {
        // Convert time to minutes since midnight
        return hourOfDay * 60 + minute;
    }


    private final RecipeClickResponseListener recipeClickListener = new RecipeClickResponseListener() {
        @Override
        public void onRecipeClick(String recipeId) {
            DetailedRecipeDialog detailedRecipeDialog = DetailedRecipeDialog.newInstance(Integer.parseInt(recipeId));
            detailedRecipeDialog.show(getParentFragmentManager(), "DetailedRecipeDialog");
        }
    };

    private void deleteExistingMealPlan(DataSnapshot snapshot) {
        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            dataSnapshot.getRef().removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Old meal plan removed successfully", Toast.LENGTH_SHORT).show();
                    Log.d("MealPlanner", "Old meal plan removed successfully");
                } else {
                    Log.d("MealPlanner", "Failed to remove old meal plan");
                }
            });
        }
    }

    private void fetchMealPlan() {
        binding.setDate.setVisibility(View.GONE);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        mealReference = db.getReference("Users").child(user.getUid()).child("mealPlan");

        mealReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mealList = new ArrayList<>();
                nutrients = new Nutrients();
                mealTimes = new String[3];
                Map<String, String> mealTimeMap = new HashMap<>();
                startDateString = "";

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String todayDate = dateFormat.format(Calendar.getInstance().getTime());

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        startDateString = dataSnapshot.child("startDate").getValue(String.class);

                        if (startDateString != null) {
                            try {
                                Date startDate = dateFormat.parse(startDateString);

                                if (startDate != null && startDate.before(today.getTime())) {
                                    Log.d("MealPlanner", "Fetched Start Date: " + startDateString);
                                    Log.d("MealPlanner", "Deleting meal plan with start date: " + startDateString);

                                    cancelScheduleNotification("Breakfast", getContext());
                                    cancelScheduleNotification("Lunch", getContext());
                                    cancelScheduleNotification("Dinner", getContext());

                                    mealTimeMap.clear();
                                    dataSnapshot.getRef().removeValue();
                                    mealTimeMap.clear();
                                    continue;
                                }

                                if (startDateString.equals(todayDate)) {
                                    // Only process the meal plan if it matches today's date
                                    mealTimeMap = (Map<String, String>) dataSnapshot.child("mealTimes").getValue();
                                    if (mealTimeMap != null) {
                                        mealTimes[0] = mealTimeMap.get("breakfast");
                                        mealTimes[1] = mealTimeMap.get("lunch");
                                        mealTimes[2] = mealTimeMap.get("dinner");

                                        // Schedule notifications for each meal time
                                        scheduleNotification("Breakfast", mealTimes[0], getContext());
                                        scheduleNotification("Lunch", mealTimes[1], getContext());
                                        scheduleNotification("Dinner", mealTimes[2], getContext());
                                    }

                                    for (DataSnapshot mealSnapshot : dataSnapshot.child("meals").getChildren()) {
                                        Meal meal = mealSnapshot.getValue(Meal.class);
                                        if (meal != null) {
                                            mealList.add(meal);
                                        }
                                    }

                                    nutrients = dataSnapshot.child("nutrients").getValue(Nutrients.class);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!mealList.isEmpty()) {
                        generateMealPlanAdapter = new GenerateMealPlanAdapter(getContext(), mealList, recipeClickListener, mealTimes);
                        generateMealPlanNutritionAdapter = new GenerateMealPlanNutritionAdapter(getContext(), nutrients);
                        binding.mealPlannerRecipeRecyclerView.setAdapter(generateMealPlanAdapter);
                        binding.mealPlannerRecipeNutritionRecyclerView.setAdapter(generateMealPlanNutritionAdapter);

                        binding.setDate.setVisibility(View.VISIBLE);
                        binding.setDate.setText(todayDate);
                    } else {
                        binding.setDate.setVisibility(View.GONE);
                    }
                } else {
                    binding.setDate.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Failed to retrieve data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelScheduleNotification(String mealType, Context context){
        if (context == null) {
            Log.e("MealPlannerFragment", "Context is null, cannot cancel notification.");
            return;
        }

        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                mealType.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            Log.d("MealPlannerFragment", "Canceled notification for: " + mealType);
        }
    }

    private void scheduleNotification(String mealType, String mealTime, Context context) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date;

        if (context == null) {
            Log.e("MealPlannerFragment", "Context is null, cannot schedule notification.");
            return;
        }

        try {
            date = inputFormat.parse(mealTime);
            if (date != null) {
                String formattedTime = outputFormat.format(date);
                String[] timeParts = formattedTime.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // If the scheduled time is in the past, set it to the next day
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                Intent intent = new Intent(context, NotificationAlarmReceiver.class);
                intent.putExtra("mealType", mealType);
                intent.putExtra("mealTime", mealTime);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        mealType.hashCode(), // Use a unique request code
                        intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
                );

                if (pendingIntent != null) {
                    Log.d("MealPlannerFragment", "Notification for " + mealType + " already exists.");
                    return;
                }

                // If no notification exists, create a new one
                pendingIntent = PendingIntent.getBroadcast(
                        context,
                        mealType.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Check Android version
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // For Android 12 (API 31) and above
                    if (alarmManager.canScheduleExactAlarms()) {
                        // If permission is granted, use exact alarm
                        try {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } catch (SecurityException e) {
                            Log.e("AlarmScheduler", "Failed to schedule exact alarm", e);
                        }
                    } else {
                        // Redirect the user to the app's settings page to allow exact alarms
                        Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        permissionIntent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(permissionIntent);

                        // Fall back to inexact repeating if permission isn't granted
                        scheduleInexactRepeatingAlarm(alarmManager, calendar, pendingIntent);
                    }
                } else {
                    // For Android 11 and below, use setExactAndAllowWhileIdle without permission checks
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Helper method to schedule inexact repeating alarm as fallback
    private void scheduleInexactRepeatingAlarm(AlarmManager alarmManager, Calendar calendar, PendingIntent pendingIntent) {
        // Set the alarm to repeat every day at the specified time
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }
}