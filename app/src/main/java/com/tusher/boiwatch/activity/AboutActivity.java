package com.tusher.boiwatch.activity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.tusher.boiwatch.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView tvNote = findViewById(R.id.tv_about_note);
        TextView tvLinks = findViewById(R.id.tv_about_links);
        View btnUpdate = findViewById(R.id.btn_check_update);

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> 
                com.tusher.boiwatch.utils.UpdateHelper.checkForUpdates(this, true)
            );
        }

        String noteText = "In our local dialect here in Bangladesh, the older generation used to lovingly refer to “movies” as “boi”. When I was brainstorming a name for this app, that familiar word came back to me.  That’s how BoiWatch was born. Even though someone very valuable to me jokingly suggested me to name it 'matflix', I felt sorry for not being able to name this app according to that individual's suggestion.<br/><br/>" +
                "Building a full-featured mobile streaming app is challenging. All the movies and shows in BoiWatch are streamed from third-party providers. I never intended to include ads or subscriptions. This app was created purely for my own enjoyment and comfort, and I wanted to keep it completely free and ad-free for everyone who uses it.<br/><br/>" +
                "However, because the content comes from external sources, those providers may occasionally display ads or pop-ups. To help reduce this, I’ve built in an ad blocker. But it’s not perfect, and some ads may still slip through from time to time. I sincerely apologize in advance if you encounter any.<br/><br/>" +
                "Since we rely on external servers, playback can sometimes be unstable or a link may stop working. If that happens, just tap the server icon in the top-right corner of the player screen to switch to another source. One of them usually works perfectly.<br/><br/>" +
                "If this little app makes it easier for even one person to enjoy movies comfortably, that will be my greatest reward. Thank you for using BoiWatch.<br/><br/>" +
                "With love,<br/>" +
                "<b>Mahatir Ahmed Tusher</b><br/>" +
                "Lead Developer, BoiWatch";

        String linksText = "📧 Email: mahatirtusher@gmail.com\n" +
                "Google Scholar: https://scholar.google.com/citations?user=k8hhhx4AAAAJ&hl=en\n" +
                "LinkedIn: https://www.linkedin.com/in/mahatir-ahmed-tusher-5a5524257\n" +
                "GitHub: https://github.com/Mahatir-Ahmed-Tusher";

        tvNote.setText(Html.fromHtml(noteText, Html.FROM_HTML_MODE_LEGACY));
        tvLinks.setText(linksText);
    }
}
