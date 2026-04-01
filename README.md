# ⚠️ CHÚ Ý QUAN TRỌNG VỀ CẤU TRÚC CODE

Mình đã tạo sẵn các packages (folder) phân chia theo từng tính năng của app. 

Ae **viết code toàn bộ vào bên trong các folder tương ứng** với phần việc của mình để không bị lỗi.

**ĐỪNG TẠO MODULE MỚI TRONG app, LỖI ĐẤY**

Mình sử dụng cấu trúc Hybird nhé, ae nào thấy khó hiểu thì cứ copy cả cái readme này bảo nó refactor lại cho
Hoặc prompt AI nnay + cấu trúc dưới: 
```text
Act as an expert Android Java Developer. >
I am working on a 5-person university fitness app project called "Fitness Tracker". We are using a Single-Activity Hybrid Architecture.
I need you to write the boilerplate Java and XML for a new screen:
[Insert Screen Name, e.g., The "Create Routine" Screen].

Here are the strict architectural rules you MUST follow:
1. The Component Rule:
If this screen needs the main bottom navigation bar visible, you MUST build it as an AndroidX Fragment.
If this screen is a full-screen active session (like a workout timer or GPS map) where the bottom nav should be hidden, you MUST build it as an AppCompatActivity.
2. The Context & View Rule (For Fragments):
Inside onCreateView, you must inflate the view and call .findViewById() on the inflated View object (do not use ViewBinding unless requested).
Whenever a Context is needed (for Room databases, Toasts, or Adapters), you MUST use requireContext() or requireActivity(). Never use this or getContext().
3. The Navigation Rule:
To navigate from one Fragment to another Fragment, do NOT use Intents. You must use: requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new NextFragment()).addToBackStack(null).commit();
To navigate from a Fragment to a breakout Activity, use a standard Intent.
4. UI Requirements:
Use ConstraintLayout as the root for the XML.
The app uses a dark theme. Use @android:color/black or @color/surface_dark for backgrounds, and @color/neon_yellow for primary buttons.
Please provide the complete Java class and the associated XML layout file. Here is what needs to be on the screen:
[Insert a brief list of UI elements, e.g., a RecyclerView for exercises, a "Save" button, and a text input for the routine name].
```

### 📁 Cấu trúc thư mục hiện tại:

```text
com.example.fitnesstracker
│
├── MainActivity.java                 <-- Khung gồm FragmentContainer & Bottom Nav
│
├── database/                         <-- SHARED DATA
│   ├── FitnessDatabase.java
│   ├── RoutineDao.java
│   ├── WorkoutDao.java
│   └── NutritionDao.java             
│
├── homescreen/                       <-- TAB 1: HomeScreen
│   ├── HomeFragment.java             
│   ├── adapters/                     
│   └── models/                       
│
├── run/                              <-- TAB 2: Run Tracking
│   ├── RunPrepFragment.java          <-- "Start Run" screen
│   ├── RunSummaryFragment.java       <-- "Run Summary" screen
│   └── ActiveRunActivity.java        <-- BREAKOUT: GPS Map
│
├── workout/                          <-- TAB 3: Workouts and Routines
│   ├── WorkoutListFragment.java      <-- Main "Pre-Set Routines" screen
│   ├── WorkoutDetailFragment.java    <-- "Hypertrophy Focus" details screen
│   ├── CreateRoutineFragment.java    <-- "Create Routine" screen
│   ├── AddExerciseFragment.java      <-- "Exercise Library" screen
│   └── ActiveWorkoutActivity.java    <-- BREAKOUT: Timers/Reps
│
├── nutrition/                        <-- TAB 4: Nutrition
│   ├── NutritionFragment.java        <-- Main Calorie & Hydration screen
│   └── LogFoodActivity.java          <-- BREAKOUT: "Recent Logs" list
│
└── profile/                          <-- TAB 5: Profiles
    ├── ProfileFragment.java          <-- Main weight/stats screen
    └── ActivityArchiveFragment.java  <-- "Activity & Wins" history screen
