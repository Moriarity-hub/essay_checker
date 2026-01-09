package com.example.essaychecker.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class CorrectionResponse {
    @SerializedName("grammar_errors")
    private List<String> grammarErrors;

    @SerializedName("fluency_suggestions")
    private List<String> fluencySuggestions;

    @SerializedName("logic_evaluation")
    private String logicEvaluation;

    @SerializedName("general_suggestions")
    private List<String> generalSuggestions;

    public List<String> getGrammarErrors() {
        return grammarErrors;
    }

    public void setGrammarErrors(List<String> grammarErrors) {
        this.grammarErrors = grammarErrors;
    }

    public List<String> getFluencySuggestions() {
        return fluencySuggestions;
    }

    public void setFluencySuggestions(List<String> fluencySuggestions) {
        this.fluencySuggestions = fluencySuggestions;
    }

    public String getLogicEvaluation() {
        return logicEvaluation;
    }

    public void setLogicEvaluation(String logicEvaluation) {
        this.logicEvaluation = logicEvaluation;
    }

    public List<String> getGeneralSuggestions() {
        return generalSuggestions;
    }

    public void setGeneralSuggestions(List<String> generalSuggestions) {
        this.generalSuggestions = generalSuggestions;
    }
}
