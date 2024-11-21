package com.example.dishdash;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishdash.databinding.ActivityTermsAndConditionsBinding;

public class TermsAndConditions extends AppCompatActivity {
    ActivityTermsAndConditionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityTermsAndConditionsBinding.inflate(getLayoutInflater());
        InsetsUtil.applyInsets(binding.getRoot());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.textView24.setOnClickListener(v -> {
            Intent intent = new Intent(this, home.class);
            intent.putExtra("NAV_TARGET", "PROFILE"); // Pass a flag to indicate the target
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        String htmlContent =
                "<p><strong>Last Updated: November 20, 2024</strong></p>\n" +
                "<p>Welcome to DishDash, a mobile application owned and operated by DishDash (\"we,\" \"us,\" or \"our\"). Please read these Terms and Conditions carefully before using the DishDash mobile application.</p>\n" +
                "\n" +
                "<h2>1. Contact Information</h2>\n" +
                "<ul>\n" +
                "<li>Email: <a href=\"mailto:maestrojomar143@gmail.com\">maestrojomar143@gmail.com</a></li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>2. Acceptance of Terms</h2>\n" +
                "<p>By downloading, installing, or using the DishDash application, you agree to be bound by these Terms and Conditions. If you do not agree to these terms, please do not use the application.</p>\n" +
                "\n" +
                "<h2>3. User Accounts</h2>\n" +
                "<h3>3.1 Account Creation</h3>\n" +
                "<ul>\n" +
                "<li>Users must create an account to use the features of the application, such as saving personal recipes, meal planning, and using preferences for dietary goals.</li>\n" +
                "<li>All account information provided must be accurate and current.</li>\n" +
                "<li>Users are responsible for maintaining the confidentiality of their account credentials.</li>\n" +
                "<li>Users must be at least 18 years old to create an account.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h3>3.2 Profile Management</h3>\n" +
                "<ul>\n" +
                "<li>Users may customize their profiles, upload profile pictures, and set preferences such as dietary needs, caloric goals, and allergies.</li>\n" +
                "<li>Users are responsible for ensuring that all information is accurate and up to date.</li>\n" +
                "<li>We reserve the right to remove any inappropriate content or profiles without notice.</li>\n" +
                "<li>Users can edit or delete their profile information at any time.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h3>3.3 Account Management</h3>\n" +
                "<ul>\n" +
                "<li>Users can log out, reset their passwords, or delete their accounts through the app.</li>\n" +
                "<li>We are not responsible for any lost data if an account is deleted by the user.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>4. Services and Features</h2>\n" +
                "<p>DishDash provides various services and features, including:</p>\n" +
                "<ul>\n" +
                "<li><strong>Recipe Database:</strong> Access to a wide variety of recipes with ingredients, instructions, and nutritional information.</li>\n" +
                "<li><strong>Personal Recipe Management:</strong> Users can add, edit, or delete personal recipes.</li>\n" +
                "<li><strong>Favorites:</strong> Users can save their favorite recipes for easy access.</li>\n" +
                "<li><strong>Meal Planner:</strong> Generate personalized meal plans based on your dietary preferences, caloric goals, and exclusions (e.g., allergies).</li>\n" +
                "<li><strong>Notifications:</strong> Receive reminders and alerts related to meal planning.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>5. Spoonacular API Integration</h2>\n" +
                "<p>DishDash uses the Spoonacular API to provide access to an extensive database of recipes, ingredients, nutritional information, and other related services. By using the app, you acknowledge and agree to the following:</p>\n" +
                "<ul>\n" +
                "<li>The Spoonacular API is used to provide recipe data and nutrition information within the app.</li>\n" +
                "<li>Spoonacular is an external service provider, and its API is subject to its own terms and conditions and privacy policy. You agree to comply with these terms when using features powered by the Spoonacular API.</li>\n" +
                "<li>DishDash is not responsible for the availability, accuracy, or functionality of the Spoonacular API. Any issues or disruptions with the API may affect certain features in the app.</li>\n" +
                "</ul>\n" +
                "<p>For more information about the Spoonacular API, please visit <a href=\"https://spoonacular.com/food-api\" target=\"_blank\">Spoonacular's official website</a>.</p>\n" +
                "\n" +
                "<h2>6. Recipe Information</h2>\n" +
                "<ul>\n" +
                "<li>The recipe information, including ingredients, nutritional facts, and instructions, is provided for informational purposes only.</li>\n" +
                "<li>While we strive for accuracy, we do not guarantee the correctness or completeness of recipe data, and we are not liable for any errors or omissions.</li>\n" +
                "<li>Nutritional information is based on standard serving sizes and may vary based on preparation methods or ingredient substitutions.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>7. Privacy and Data Protection</h2>\n" +
                "<ul>\n" +
                "<li>We collect and process personal information in accordance with our <a href=\"link-to-privacy-policy\">Privacy Policy</a>.</li>\n" +
                "<li>User data, including dietary preferences and saved recipes, is stored securely and used only for the purpose of providing services within the app.</li>\n" +
                "<li>Users may request to view, update, or delete their personal information at any time.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>8. Modifications to Service</h2>\n" +
                "<p>We reserve the right to:</p>\n" +
                "<ul>\n" +
                "<li>Modify, update, or discontinue any aspect of the service at any time.</li>\n" +
                "<li>Add new features or remove existing ones.</li>\n" +
                "<li>Change pricing policies, where applicable.</li>\n" +
                "<li>Users will be notified of significant changes through app notifications or other means.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>9. Termination</h2>\n" +
                "<p>We reserve the right to:</p>\n" +
                "<ul>\n" +
                "<li>Suspend or terminate user accounts that violate these Terms and Conditions.</li>\n" +
                "<li>Block access to users who engage in fraudulent or abusive activities.</li>\n" +
                "<li>Delete user accounts and all related data in case of prolonged inactivity or violation of the terms.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>10. Limitation of Liability</h2>\n" +
                "<ul>\n" +
                "<li>DishDash is not responsible for any loss, damage, or injury arising from the use of the app, including but not limited to issues with recipes, meal planning, or external services used in conjunction with the app.</li>\n" +
                "<li>We are not liable for any delays in notifications or the unavailability of specific features.</li>\n" +
                "<li>Users are responsible for their own health and safety when following recipes or using meal plans.</li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>11. Contact</h2>\n" +
                "<p>For questions or concerns regarding these Terms and Conditions, please contact us at:</p>\n" +
                "<ul>\n" +
                "<li>Email: <a href=\"mailto:maestrojomar143@gmail.com\">maestrojomar143@gmail.com</a></li>\n" +
                "</ul>\n" +
                "\n" +
                "<h2>12. Agreement</h2>\n" +
                "<p>By using the DishDash application, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions.</p>";

        binding.termsAndConditions.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT));

        binding.termsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());
    }
}