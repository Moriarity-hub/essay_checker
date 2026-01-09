package com.example.essaychecker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.essaychecker.api.ApiService;
import com.example.essaychecker.model.CorrectionResponse;
import com.example.essaychecker.model.EssayRequest;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private EditText etEssayInput;
    private Button btnSubmit;
    private TextView tvResultContent;
    private TextView tvLoadingStatus;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEssayInput = findViewById(R.id.et_essay_input);
        btnSubmit = findViewById(R.id.btn_submit);
        tvResultContent = findViewById(R.id.tv_result_content);
        tvLoadingStatus = findViewById(R.id.tv_loading_status);

        // 创建自定义 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request req = chain.request();
                    long t1 = System.nanoTime();
                    okhttp3.Response response = chain.proceed(req);
                    long t2 = System.nanoTime();
                    Log.d("OKHTTP", String.format("→ %s  %.1f ms%n%s",
                            req.url(), (t2 - t1) / 1e6, req.headers()));
                    return response;
                })
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // 使用这个 client 初始化 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .client(client)  // 👈 关键：设置自定义 client
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String essayContent = etEssayInput.getText().toString().trim();

                if (essayContent.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入作文内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                startCorrectionProcess(essayContent);
            }
        });
    }

    private void startCorrectionProcess(final String essayContent) {
        tvLoadingStatus.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        tvResultContent.setText("");

        EssayRequest request = new EssayRequest(essayContent);
        Call<CorrectionResponse> call = apiService.correctEssay(request);

        call.enqueue(new Callback<CorrectionResponse>() {
            @Override
            public void onResponse(Call<CorrectionResponse> call, Response<CorrectionResponse> response) {
                tvLoadingStatus.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    CorrectionResponse correction = response.body();
                    String formattedResult = formatResult(correction);
                    tvResultContent.setText(formattedResult);
                    Toast.makeText(MainActivity.this, "批改完成！", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API_CALL_ERROR", "Response not successful: " + response.code() + " " + response.message());
                    Toast.makeText(MainActivity.this, "批改失败，请重试。", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CorrectionResponse> call, Throwable t) {
                tvLoadingStatus.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Log.e("API_CALL_FAILURE", "Network failure", t);
                Toast.makeText(MainActivity.this, "网络连接失败，请检查服务器。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatResult(CorrectionResponse correction) {
        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append("语法错误:\n");
        if (correction.getGrammarErrors() != null && !correction.getGrammarErrors().isEmpty()) {
            for (String error : correction.getGrammarErrors()) {
                resultBuilder.append("- ").append(error).append("\n");
            }
        } else {
            resultBuilder.append("  无\n");
        }

        resultBuilder.append("\n语句通顺建议:\n");
        if (correction.getFluencySuggestions() != null && !correction.getFluencySuggestions().isEmpty()) {
            for (String suggestion : correction.getFluencySuggestions()) {
                resultBuilder.append("- ").append(suggestion).append("\n");
            }
        } else {
            resultBuilder.append("  无\n");
        }

        resultBuilder.append("\n逻辑评估:\n");
        if (correction.getLogicEvaluation() != null) {
            resultBuilder.append(correction.getLogicEvaluation()).append("\n");
        }

        resultBuilder.append("\n综合提升建议:\n");
        if (correction.getGeneralSuggestions() != null && !correction.getGeneralSuggestions().isEmpty()) {
            for (String suggestion : correction.getGeneralSuggestions()) {
                resultBuilder.append("- ").append(suggestion).append("\n");
            }
        } else {
            resultBuilder.append("  无\n");
        }

        return resultBuilder.toString();
    }
}
