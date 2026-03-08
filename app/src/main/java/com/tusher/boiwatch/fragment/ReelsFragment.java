package com.tusher.boiwatch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.ShortsAdapter;
import com.tusher.boiwatch.models.ShortItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReelsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShortsAdapter adapter;
    private List<ShortItem> shortList = new ArrayList<>();
    private int currentPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reels, container, false);
        recyclerView = view.findViewById(R.id.rv_reels);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadShorts();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        if (recyclerView == null || shortList == null) return;

        adapter = new ShortsAdapter(shortList, position -> {
            if (recyclerView == null || shortList == null) return;
            int nextPosition = position + 1;
            if (nextPosition < shortList.size()) {
                recyclerView.smoothScrollToPosition(nextPosition);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // TikTok-style full-screen snapping
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        if (position != currentPosition) {
                            currentPosition = position;
                            playAtPosition(position);
                        }
                    }
                }
            }
        });

        // Initial play
        recyclerView.post(() -> {
            if (recyclerView != null) playAtPosition(0);
        });
    }

    private void playAtPosition(int position) {
        if (recyclerView == null || shortList == null || position < 0 || position >= shortList.size()) return;
        
        // Auto-pause background audiobook if playing
        if (getContext() != null) {
            android.content.Intent pauseIntent = new android.content.Intent("ACTION_AUDIOBOOK_CONTROL");
            pauseIntent.putExtra("cmd", "pause");
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getContext()).sendBroadcast(pauseIntent);
        }

        // Find the ViewHolder and trigger its internal play logic
        ShortsAdapter.ViewHolder holder = (ShortsAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            holder.play();
        }
    }

    private void loadShorts() {
        shortList.add(new ShortItem("uvX-P8iViIo", "28 Years Later quick review", "Quick look at the upcoming sequel!", "BoiWatch Curated"));
        shortList.add(new ShortItem("4URYl_mSGNo", "Viral movie explain short", "This goes viral every time!", "Movie Explained"));
        shortList.add(new ShortItem("mUML_2lL7_A", "Horror movie funny moment", "Wait for the twist...", "Cinema Funny"));
        shortList.add(new ShortItem("dGc1z2qRsKQ", "Fantasy movie recap short", "Magic and monsters!", "Fantasy Recap"));
        shortList.add(new ShortItem("HDQPjTx64FM", "Funniest Scary Movie Moments", "Classic horror comedy.", "Scary Movie"));
        shortList.add(new ShortItem("6o1l9dWYKqE", "An American Werewolf In London recap", "The transformation scene!", "Classic Horror"));
        shortList.add(new ShortItem("BiMGL8CLfJs", "If You Move You Die", "Westworld intensity.", "Sci-Fi Recap"));
        shortList.add(new ShortItem("Ldbv-fUo9HM", "Parents living action movie plots", "Everyday hero moments.", "Action Comedy"));
        shortList.add(new ShortItem("7FEE7_A6Xdo", "10 Best Plot Twists", "Did you see these coming?", "Movie Magic"));
        shortList.add(new ShortItem("Xzliw6zOvQI", "16 Best Plot Twists", "Mind-blowing endings.", "Cinema Twist"));
        shortList.add(new ShortItem("MUbBEyahrzU", "Best plot twists question", "What is yours?", "Talk Cinema"));
        shortList.add(new ShortItem("Kvp-z92Emy4", "Best Plot Twist Movies", "Highly recommended list.", "Must Watch"));
        shortList.add(new ShortItem("CGcdZb3gqe8", "10 Best Movies with Twist Ending", "The ultimate collection.", "Top 10 Cinema"));
        shortList.add(new ShortItem("XLY4_dOcMTY", "Full Movie Explained in 1 Minute", "Fast recap style.", "Movie Recap"));
        shortList.add(new ShortItem("k2acw5A7iuM", "The Sixth Sense Reveal", "How ONE ring revealed the twist.", "Plot Twist"));
        shortList.add(new ShortItem("hyG46mgNDAg", "He's All That Review", "Netflix TikTok star movie.", "Netflix Review"));
        shortList.add(new ShortItem("LuDfUMzs2S4", "Christopher Nolan Fun Facts", "Mind-bending director facts.", "Nolan Fans"));
        shortList.add(new ShortItem("eeDyz9HWzJg", "Starz Recapped", "Movie Explained In English.", "Movie Recap"));
        shortList.add(new ShortItem("_WB_Rixyfls", "The Substance Movie Recap", "Quick breakdown.", "Movie Recap"));
        shortList.add(new ShortItem("xqwR5C2o52M", "BIGGEST PLOT TWIST EVER", "You won't believe this ending.", "Movie Twist"));
        shortList.add(new ShortItem("Ms_GWVAJ7Uw", "28 Years Later- The Bone Temple", "2026 Recap | Ending Explained.", "Ending Explained"));
        shortList.add(new ShortItem("TZqyJDTUuus", "Plot Twist We Totally Missed", "#InYourDream #Netflix2025.", "Netflix Fans"));
        shortList.add(new ShortItem("tPaIsGfFNZ0", "South Indian movie best scene", "Catchy explanation style.", "South Cinema"));
        shortList.add(new ShortItem("clibtWDXPx4", "Twilight: The Big Movie Recap", "Hilarious Parody & Epic Moments.", "Cinema Parody"));
        shortList.add(new ShortItem("3iGqR9z8xOg", "NO ONE EXPECTED THIS!", "Must Watch Movie Twist.", "Shocking Endings"));
        shortList.add(new ShortItem("9vyTirJGCRo", "Ace Ventura: When Nature Calls", "FULL MOVIE RECAP & REVIEW.", "Comedy Recap"));
        shortList.add(new ShortItem("NTJlUINdWHc", "Jaw-Dropping Movie Plot Twists", "Leave you speechless!", "Movie Magic"));
        shortList.add(new ShortItem("7Rb7lxbBuTo", "Movie Scene Explained in 30s", "You Won't Expect the Twist!", "Quick Explain"));
        shortList.add(new ShortItem("fEngc6L1fgk", "Did you know The Usual Suspects...", "Funny twist fact.", "Did You Know"));
        shortList.add(new ShortItem("MqlpTIIJVOU", "Same Scene, Different Movies", "Cinematic Parallels.", "Film Parallel"));
        shortList.add(new ShortItem("R5QVdpgbX4A", "Most shocking movie plot twists", "The ultimate list.", "Top Shockers"));
        shortList.add(new ShortItem("sx5QQ8Q4akI", "21 Jump Street Best Scene", "Sequel funny moments.", "Action Comedy"));
        shortList.add(new ShortItem("KEJ0fSQIbhM", "CHAIN OF COMMAND (1994)", "BIGGEST PLOT TWIST EVER!", "Classic Twist"));
        shortList.add(new ShortItem("YpzDNKL0I1M", "Top Secret! funniest moments", "Classic comedy.", "Funny Movies"));
        shortList.add(new ShortItem("plDquEuM8AA", "Steven Seagal Best Scene!", "Action movie funny moment.", "Action Hero"));
        shortList.add(new ShortItem("nXD-fDebgds", "Biggest plot twist in history", "The Usual Suspects breakdown.", "Cinema History"));
        shortList.add(new ShortItem("c44gMXVLNVY", "Twist Villains", "Writing tips with Batman example.", "Script Writing"));
        shortList.add(new ShortItem("vGYZBH4NOS8", "Every movie recap music ever", "Funny meme moment.", "Cinema Memes"));
        shortList.add(new ShortItem("lxqJCof98Fc", "ET Phone Home", "Iconic cinematic scene.", "Iconic Moments"));
        shortList.add(new ShortItem("HnTmIK_JaUU", "Dwayne Johnson & Jamie Foxx", "Funny moments together.", "Celebrity Fun"));
        shortList.add(new ShortItem("69UYA-aFCZ8", "The Ridiculous 6", "I Got a Strong Neck!", "Happy Madison"));
        shortList.add(new ShortItem("bwhWf6HFSyY", "Kantara Climax Scene", "Animated Spoof version.", "Animated Fun"));
        shortList.add(new ShortItem("tnI70wXeAMU", "THE EQUALIZER 4", "Upcoming teaser short.", "Teaser News"));
        shortList.add(new ShortItem("PnJl8lTFk6M", "RUSH HOUR 4", "Action highlights.", "Action Movies"));
        shortList.add(new ShortItem("HtZcmIOvuos", "That Plot Twist Though", "Short reaction.", "Twist reaction"));
        shortList.add(new ShortItem("9jj0oS7oOF4", "The Monkey (2025)", "Horror Movie Recap Short.", "Horror 2025"));
        shortList.add(new ShortItem("0yfqLS1I60A", "Waktu Maghrib", "Horror Movie Recaps.", "Asian Horror"));
        shortList.add(new ShortItem("DAC07UELp60", "When Evil Lurks", "Horror Movie Recap.", "Modern Horror"));
        shortList.add(new ShortItem("MVqyS_EG3us", "Scary Movie Recap", "General plot breakdown.", "Scary Recap"));
        shortList.add(new ShortItem("u3aEAEfCktY", "Dead Silence Explained", "Explained in 60 Seconds.", "60s Explain"));
        shortList.add(new ShortItem("Y23xL-5RMvk", "What Is Nerd Horror?", "Explained in 60 Seconds.", "Horror Trivia"));
        shortList.add(new ShortItem("89sIjOiuRAg", "Universal Halloween Horror", "Explained in 60 Seconds.", "Horror Events"));
        shortList.add(new ShortItem("So9ciVLxWGw", "Monster Movies Explained", "Explained in 60 Seconds.", "Monster World"));
        shortList.add(new ShortItem("ZsPWnu-e294", "INSANE Plot Twists", "Horror Movies collection.", "Insane Twists"));
        shortList.add(new ShortItem("0fHzhtD9gpk", "Horror Twist Endings", "Best of the best.", "Twist Endings"));
        shortList.add(new ShortItem("WKMKVrLqGEc", "FUNNIEST MOVIE SCENES", "Of all time collection.", "Movie Fun"));
        shortList.add(new ShortItem("ZKyjBJ8uay0", "The Greatest Story Ever!", "Despicable Me moment.", "Minion Fun"));
        shortList.add(new ShortItem("6Y16fK3Q6RM", "Oh and Tip's NEW CAR!", "Home movie funny clip.", "Animated Comedy"));
        shortList.add(new ShortItem("kX4ZtkhZfmo", "Miles visit girlfriend", "Martin Lawrence comedy.", "Martin Lawrence"));
        shortList.add(new ShortItem("PAcvtCq3Xhg", "Comedy Movie Recap", "Funniest Scenes (Happy Ghost 2).", "Asian Comedy"));
        shortList.add(new ShortItem("6fk0wMiRtos", "Scarface Dinner Scene", "Powerful and funny.", "Classic Scarface"));
        shortList.add(new ShortItem("lZv1Tv9OyWw", "Mr Cutty Knife", "Peep Show comedy clip.", "UK Comedy"));
        shortList.add(new ShortItem("80N3P3mBn4A", "Someone Help Him", "The Middle sitcom fun.", "Sitcom Life"));
        shortList.add(new ShortItem("Nkr-QHILhcA", "He Was So Desperate", "Mom sitcom moment.", "Mom Sitcom"));
        shortList.add(new ShortItem("vADmAePpzVc", "Shoot Your Shot", "The Middle moment.", "The Middle"));
        shortList.add(new ShortItem("i1VEQULqq5I", "Get Him Outta There", "Mom sitcom clip.", "Mom TV"));
        shortList.add(new ShortItem("gREIB3co_RA", "Too Good to Pass Up", "The Middle sitcom.", "Sitcom Clips"));
        shortList.add(new ShortItem("UhQ3QR4gFMY", "Make Dream Come True", "Mom sitcom ending.", "Mom Series"));
        shortList.add(new ShortItem("kCIERGJg7h0", "Christopher Nolan ranking", "Best movies ranked.", "Nolan ranking"));
        shortList.add(new ShortItem("dpatX2qQXak", "Nolan Must-Watch", "Might Surprise You.", "Director Choice"));
        shortList.add(new ShortItem("X_E_rCsD7dw", "Blind Ranking Nolan", "Ranking director movies.", "Director Ranking"));
        shortList.add(new ShortItem("xthPZxM2XE8", "Ranking Nolan's Movies", "Hot takes edition.", "Director Hot Takes"));
        shortList.add(new ShortItem("1WZgeqioKr8", "Christopher Nolan new movie", "Comedy short version.", "Director News"));
        shortList.add(new ShortItem("1thMs9QhgA0", "Jack Black & Paul Rudd", "Their favorite Nolan movies.", "Celeb Choice"));
        shortList.add(new ShortItem("Zux72tzc18A", "Top 5 Nolan Films", "The definitive list.", "Top 5 movies"));
        shortList.add(new ShortItem("SgF00sY427Q", "Nolan's Greatest Moments", "Without Special Effects.", "Practical Effects"));
        shortList.add(new ShortItem("LuDfUMzs2S4", "Nolan Fun Facts", "Mind-blowing trivia.", "Nolan Facts"));
        shortList.add(new ShortItem("LZ4RX6qHyYk", "Amazing Nolan Fact", "Did you know this?", "Nolan Trivia"));
        shortList.add(new ShortItem("eEM04vsgWVE", "Surprising Nolan Facts", "More amazing trivia.", "Surprise Facts"));
        shortList.add(new ShortItem("7ryvb7zEW1c", "Christopher Nolan Facts", "Deep dive trivia.", "Director Facts"));
        shortList.add(new ShortItem("IigGrQmElSI", "Summer Romance Scenes", "Best romantic moments.", "Romance collection"));
        shortList.add(new ShortItem("6qS025u9Db8", "Romantic Holiday Scenes", "Part 1.", "Holiday Romance"));
        shortList.add(new ShortItem("sj5mmSSKAgY", "Most watched shorts 2022", "#viral #hydraulicpress.", "Viral Pressure"));
        shortList.add(new ShortItem("wOfQV8DVxlA", "Top 3 Shorts 2024", "Ultimate Crushing Compilation.", "Crushing Compilation"));
        shortList.add(new ShortItem("2BzlyFn2d8U", "Tomatoes vs Press", "150 ton hydraulic press.", "Pressure Test"));
        shortList.add(new ShortItem("5djdxE49kF0", "Crushing Scrub Daddy", "150 tons hydraulic press.", "Scrub Daddy"));
        shortList.add(new ShortItem("44kzO9g5YG0", "Dangerous Crush", "300 Ton Press Mayhem!", "Danger Pressure"));
        shortList.add(new ShortItem("dOU8taxvRw8", "Heat vs Steel", "Hydraulic Press Test!", "Science vs Pressure"));
        shortList.add(new ShortItem("v7-VkHFrqY0", "Pushing Orange", "Through Tiny Holes.", "Satisfying Crushing"));
        shortList.add(new ShortItem("oMVMGKNn2Eo", "Dangerous Moments", "Under Pressure compilation.", "Pressure World"));
        shortList.add(new ShortItem("2D5dHcLvkx4", "POV: Happy Cabbage", "Having the Worst Day.", "Cabbage POV"));
        shortList.add(new ShortItem("ed4_oOWLMM0", "Perfect Fit", "Under Extreme Pressure!", "Satisfying fit"));
        shortList.add(new ShortItem("3xLcYlJQF7g", "ASMR - HARD MODE", "Tasting Test.", "ASMR Tastes"));
        shortList.add(new ShortItem("zZwY8Wxc6Ps", "ASMR Triggers", "Created by creator.", "ASMR Creators"));
        shortList.add(new ShortItem("EAr8014rLkA", "ASMR Triggers Fix", "Better days edition.", "ASMR World"));
        shortList.add(new ShortItem("lt7sAyeFuNY", "ASMR Color Wheel", "So satisfying.", "Satisfying ASMR"));
        shortList.add(new ShortItem("iW0Ia1F30tQ", "Try This Sleep Hack!", "From Jojo's ASMR.", "Sleep ASMR"));
        shortList.add(new ShortItem("WwciUndTuJY", "ASMR Triggers bigger", "Growing triggers.", "Big ASMR"));
        shortList.add(new ShortItem("fES7gVqV6Ho", "Wearing Earphones?", "ASMR ear test.", "Earphone ASMR"));
        shortList.add(new ShortItem("5DIiI658v9A", "Top 5 Visual Triggers", "Visual ASMR.", "Visual Triggers"));
        shortList.add(new ShortItem("G_XE8tsil6k", "ASMR University", "Who should come?", "ASMR Uni"));
        shortList.add(new ShortItem("jh6qYc2q9Ro", "Scientifically Fall Asleep", "Quickly edition.", "Sleep Science"));
        shortList.add(new ShortItem("OQ9H1Ul_314", "7 Fascinating Nolan Facts", "Must know facts.", "Nolan Top 7"));
        shortList.add(new ShortItem("3zkbzy-gYEo", "Nolan's The Odyssey", "Most INSANE Fact.", "Nolan Odyssey"));
        shortList.add(new ShortItem("WOouL4KruCA", "Why Nolan is Greatest", "Director breakdown.", "Nolan breakdown"));
        shortList.add(new ShortItem("Dg68H_X13fM", "Nolan's next movie", "EXPLAINED breakdown.", "Nolan 2026"));
        shortList.add(new ShortItem("tT74El7tV5M", "The SECRET of Nolan", "Movie Blockbusters secret.", "Nolan Secret"));
        shortList.add(new ShortItem("AA4HVNdS3Zo", "Nolan Explains Tenet", "Finally explains logic.", "Tenet Explained"));
        shortList.add(new ShortItem("8TlbI_yrG9Q", "Inception Ending Mystery", "Nolan solves mystery.", "Inception solved"));
        shortList.add(new ShortItem("XjrkCeAzvVM", "Nolan's Biggest Film", "The Odyssey breakdown.", "Nolan Filmography"));
        shortList.add(new ShortItem("z3hXUkiGZwI", "Master of Mind-Bending", "Christopher Nolan study.", "Film Master"));
        shortList.add(new ShortItem("lVlkHqLTwNo", "Oppenheimer Scare", "I don't need words...", "Nolan Horror"));
        shortList.add(new ShortItem("c96-cpbVgNM", "Still Confused by TENET?", "Let Nolan Explain.", "Tenet Help"));
        shortList.add(new ShortItem("7lkTulspTDc", "No Deleted Scenes", "Nolan's Movies explained.", "No Cut Scenes"));
        shortList.add(new ShortItem("t8PvafC9zFs", "Memento Timeline", "Christopher Nolan breakdown.", "Memento Explained"));
        shortList.add(new ShortItem("wk223w113A0", "Nolan Red Paper Script", "Secret Revealed.", "Nolan Scripts"));
        shortList.add(new ShortItem("ZXk3rYxq3aA", "NEXT MOVIE EXPLAINED", "CHRISTOPHER NOLAN news.", "Latest Nolan"));
        shortList.add(new ShortItem("0WfG8LW0eWU", "Hidden detail in Memento", "Nolan and his movies.", "Hidden Details"));
        shortList.add(new ShortItem("MDp23ckgSTI", "End of INCEPTION", "Christopher Nolan on the end.", "Inception end"));
        shortList.add(new ShortItem("7sSWTe_fJn0", "Nolan's biggest mistake", "The Prestige short fact.", "Prestige fact"));
        shortList.add(new ShortItem("RD6ORVG5rEo", "Confusing Nolan Movies", "Explained (clip/short style).", "Nolan Explain"));

        // New Additions: Dostoevsky & Kafka
        shortList.add(new ShortItem("1qqfop2ySOg", "Notes from the Underground | Fyodor Dostoevsky", "Masterpiece Explained.", "Dostoevsky Cinema"));
        shortList.add(new ShortItem("Q0NARGUQkzk", "Crime and Punishment by Dostoevsky", "Explained in short.", "Great Books"));
        shortList.add(new ShortItem("Xml_eEvnY1I", "Crime and Punishment Summary", "Theme & Characters Explained.", "Literature Hub"));
        shortList.add(new ShortItem("tusDLRqyeRM", "Dostoevsky's Masterpiece", "Masterpiece Explained.", "Books Explained"));
        shortList.add(new ShortItem("KpTTZ3FF1jw", "Crime and Punishment - 40s", "Moral Question Explained.", "Quick Lit"));
        shortList.add(new ShortItem("soFBJPsDhdM", "Crime and Punishment Chapter 1", "⚖️ Explained!", "Dostoevsky Series"));
        shortList.add(new ShortItem("3WAO9_JrOF8", "The Book That Breaks Your Mind", "Mind-bending literature.", "Book Therapy"));
        
        shortList.add(new ShortItem("SvT5FiucjyM", "Kafka’s Metamorphosis - 60s", "Summary & Analysis.", "Kafkaesque"));
        shortList.add(new ShortItem("siZ6h5b8-_g", "Metamorphosis Summary", "Franz Kafka Explained.", "Author Studies"));
        shortList.add(new ShortItem("5ILpxguHMcQ", "The Trial by Franz Kafka", "1 Minute Summary.", "Kafka Hub"));
        shortList.add(new ShortItem("81K2WUtozLc", "The Metamorphosis Summary", "In 1 Minute.", "Book Summaries"));
        shortList.add(new ShortItem("8c2ypfLfr40", "Grete's Decision", "Metamorphosis Part 6.", "Kafka Series"));
        shortList.add(new ShortItem("6PU-wKdVhzg", "Kafka's Profound Quotes", "Essence of Metamorphosis.", "Quote Theory"));

        // Mike Tyson & Literature
        shortList.add(new ShortItem("bD1un-Dksbk", "When You're Favored By God", "Mike Tyson's Wise Words.", "Tyson Clips"));
        shortList.add(new ShortItem("f58IDHFWrC4", "Stephen A. Smith on Friends", "Mike Tyson Podcast clip.", "Hotboxin Clips"));
        shortList.add(new ShortItem("hcPUoxTvw5g", "Tyson - Rogan Highlights", "JRE #1532.", "JRE Highlights"));
        shortList.add(new ShortItem("tRRMhk-b9DA", "Mike Tyson Funny Moments", "JRE #1805.", "Podcast Comedy"));

        shortList.add(new ShortItem("IBOMDYv1Bus", "What is Classic Literature?", "Quick Explanation.", "Literature 101"));
        shortList.add(new ShortItem("Q2hK29Lb584", "WHAT IS GREAT LITERATURE?", "Deep dive question.", "Cerebral Books"));
        shortList.add(new ShortItem("lMkoUPcBfj8", "Symbolism in Literature", "Explained in 60s.", "Book Symbology"));
        shortList.add(new ShortItem("nOoV3NCysf8", "Literature Meaning Explained", "Real Example.", "Word Power"));
        shortList.add(new ShortItem("706gd_h2KPU", "English Literature Explanation", "Complete breakdown.", "General Lit"));

        // MrBeast & Entertainment
        shortList.add(new ShortItem("_sBRk9ADpK8", "Flip a Coin, Win $30k", "Beast Challenge.", "MrBeast"));
        shortList.add(new ShortItem("kNLr-cduzuo", "Wait... Was He Famous?", "Viral encounter.", "Beast Funny"));
        shortList.add(new ShortItem("0IJulOmiz28", "Ages 1-100 Try Lunchly", "Food Test.", "Beast Reaction"));
        shortList.add(new ShortItem("wSh8fAFWEWs", "Survivor vs Beast Games", "Ultimate survival.", "Beast Challenge"));
        shortList.add(new ShortItem("3GNyw4uaAqU", "Subscribe for an iPhone", "Giveaway moment.", "MrBeast Gaming"));
        shortList.add(new ShortItem("ywIcdSNJolo", "Pass the Phone", "MrBeast Team fun.", "Beast Vibes"));
        shortList.add(new ShortItem("mktYW177p6U", "Guess the Animal", "Win big!", "Beast Games"));
        shortList.add(new ShortItem("WROMyJAJmp4", "Can I Beat An F1 Driver?", "Speed test.", "Beast Speed"));
        shortList.add(new ShortItem("tAwzBYE18Us", "How I Became MrBeef", "Origin story?", "Beast Meme"));
        shortList.add(new ShortItem("LyFMbY-c_Lc", "I Raced Noah Lyles", "World's fastest race.", "Beast Athletics"));

        // Travel & Lifestyle
        shortList.add(new ShortItem("SanmT-UwqgY", "Tuscany Cascades, Italy", "Nature's beauty.", "Travel Life"));
        shortList.add(new ShortItem("LHV3dAtqL68", "Claw Machine in Japan", "Frenzy moment.", "Japan Travels"));
        shortList.add(new ShortItem("oEWU5SrMbw0", "Grandover Resort Garden", "Garden walk.", "resort Vibe"));
        shortList.add(new ShortItem("y2PCJogPsi4", "Travel Me & You", "Fun travel clip.", "Vlog Gems"));
        shortList.add(new ShortItem("SCRiXCpYGVU", "Travel Vibes with Friends", "Mini Vlog.", "Friendship Goals"));
        shortList.add(new ShortItem("i-MnihVyeuY", "Girls Trip to Scottsdale", "Travel Vlog.", "Girl Trip"));
        shortList.add(new ShortItem("9Dgo5fVUpIc", "Adventure Vibes", "Best Travel Shorts.", "Wanderlust"));
        shortList.add(new ShortItem("jWUdyVMzSeY", "Welcome to Japan", "Travel Japan Intro.", "TravelShorts"));

        // Filmmaking & Photography
        shortList.add(new ShortItem("LH6nWQRlfUY", "5 Short Film Tips", "Success guide.", "Filmmaker Pro"));
        shortList.add(new ShortItem("GJUu-ygLABI", "BT Scenes of Film", "Making of a short.", "BTS Cinema"));
        shortList.add(new ShortItem("bzxPhQO-VS4", "Create Your First Film", "Tutorial breakdown.", "Film School"));
        shortList.add(new ShortItem("dqN6uyQNLg8", "Make a Short Film", "Full process.", "Director's Cut"));
        shortList.add(new ShortItem("A7qZ23Usxso", "Andy To BTS", "Short Film Secrets.", "Andy To Philms"));
        shortList.add(new ShortItem("8DD3zGzHL30", "Making It Big", "Ultimate Guide.", "Shorts Success"));
        shortList.add(new ShortItem("PWRRMIkCrWs", "Edit Like a Pro", "Short Film edit.", "Edit Theory"));
        shortList.add(new ShortItem("zKdSxRMVzH8", "Pre-Production Process", "Andy To's method.", "Production Pro"));
        shortList.add(new ShortItem("-l-uvHcjKMQ", "Short Filmmaking Gear", "The perfect kit.", "Filmmaker Gear"));
        shortList.add(new ShortItem("jnB6Tqu93oc", "7 Reasons to Make a Film", "Inspiration for you.", "Film Motivation"));

        shortList.add(new ShortItem("0YzX_SoTjf4", "Perfect Camera Settings", "Photography basics.", "Camera Pro"));
        shortList.add(new ShortItem("Q8-hYTedlMg", "Master Your Camera", "In seconds!", "Quick Tips"));
        shortList.add(new ShortItem("L7N8U7emCVU", "5 Better Photo Tips", "Instantly improve.", "Photo Magic"));
        shortList.add(new ShortItem("pSxF-pcE0b4", "Interior Photo Settings", "Best Camera tips.", "Interior Design"));
        shortList.add(new ShortItem("NEmHPYhG2QA", "60s Photo Tips", "Photography Shorts.", "Snap School"));
        shortList.add(new ShortItem("LdwIDv0uLks", "Photography #Shorts", "Anthony Gugliotta tips.", "Gugliotta Photo"));
        shortList.add(new ShortItem("EZg1kbK-cpc", "YouTube Shorts Camera", "Full Guide.", "Camera Hub"));
        
        // Randomize the order so users see different reels each time
        Collections.shuffle(shortList);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            stopAllReels();
        } else {
            resumeCurrentReel();
        }
    }

    @Override
    public void onDestroyView() {
        stopAllReels();
        super.onDestroyView();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAllReels();
    }

    @Override
    public void onResume() {
        super.onResume();
        // ONLY resume if the fragment is actually visible to the user
        if (!isHidden()) {
            resumeCurrentReel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAllReels();
    }

    /**
     * Aggressively stops ALL Reels playback. Pauses and recycles every
     * visible ViewHolder so no WebView continues playing in the background.
     */
    private void stopAllReels() {
        if (recyclerView == null) return;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (vh instanceof ShortsAdapter.ViewHolder) {
                ((ShortsAdapter.ViewHolder) vh).pause();
                ((ShortsAdapter.ViewHolder) vh).recycle();
            }
        }
    }

    /**
     * Resumes only the currently visible Reel (if any).
     */
    private void resumeCurrentReel() {
        if (recyclerView != null && currentPosition >= 0) {
            ShortsAdapter.ViewHolder holder = (ShortsAdapter.ViewHolder) 
                    recyclerView.findViewHolderForAdapterPosition(currentPosition);
            if (holder != null) {
                holder.play();
            }
        }
    }
}

