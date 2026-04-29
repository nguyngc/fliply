package model.entity;

import jakarta.persistence.*;

import java.util.Locale;
import java.util.Map;

@Entity
@Table(name = "FLASHCARD")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FlashcardId")
    private Integer flashcardId;

    @Column(name = "Term")
    private String term;

    @Column(name = "Definition")
    private String definition;

    @Column(name = "DefinitionAr")
    private String definitionAr;

    @Column(name = "DefinitionFi")
    private String definitionFi;

    @Column(name = "DefinitionKo")
    private String definitionKo;

    @Column(name = "DefinitionLo")
    private String definitionLo;

    @Column(name = "DefinitionVi")
    private String definitionVi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FlashcardSetId", referencedColumnName = "FlashcardSetId", nullable = true)
    private FlashcardSet flashcardSet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", referencedColumnName = "UserId", nullable = false)
    private User user;

    public Flashcard() {}

    public Flashcard(String term, String definition, FlashcardSet flashcardSet, User user) {
        this.term = term;
        this.definition = definition;
        this.flashcardSet = flashcardSet;
        this.user = user;
    }

    public Integer getFlashcardId() { return flashcardId; }
    public String getTerm() { return term; }
    public String getDefinition() { return definition; }
    public String getDefinitionAr() { return definitionAr; }
    public String getDefinitionFi() { return definitionFi; }
    public String getDefinitionKo() { return definitionKo; }
    public String getDefinitionLo() { return definitionLo; }
    public String getDefinitionVi() { return definitionVi; }
    public FlashcardSet getFlashcardSet() { return flashcardSet; }
    public User getUser() { return user; }

    public void setTerm(String term) { this.term = term; }
    public void setDefinition(String definition) { this.definition = definition; }
    public void setDefinitionAr(String definitionAr) { this.definitionAr = definitionAr; }
    public void setDefinitionFi(String definitionFi) { this.definitionFi = definitionFi; }
    public void setDefinitionKo(String definitionKo) { this.definitionKo = definitionKo; }
    public void setDefinitionLo(String definitionLo) { this.definitionLo = definitionLo; }
    public void setDefinitionVi(String definitionVi) { this.definitionVi = definitionVi; }
    public void setFlashcardSet(FlashcardSet flashcardSet) { this.flashcardSet = flashcardSet; }
    public void setUser(User user) { this.user = user; }

    public void setDefinitions(Map<String, String> definitions) {
        if (definitions == null) {
            return;
        }
        setDefinition(textOrNull(definitions.get("en")));
        setDefinitionAr(textOrNull(definitions.get("ar")));
        setDefinitionFi(textOrNull(definitions.get("fi")));
        setDefinitionKo(textOrNull(definitions.get("ko")));
        setDefinitionLo(textOrNull(definitions.get("lo")));
        setDefinitionVi(textOrNull(definitions.get("vi")));
    }

    public String getLocalizedDefinition(String language) {
        String selected = switch (normalizeLanguage(language)) {
            case "ar" -> definitionAr;
            case "fi" -> definitionFi;
            case "ko" -> definitionKo;
            case "lo" -> definitionLo;
            case "vi" -> definitionVi;
            default -> definition;
        };
        return isBlank(selected) ? definition : selected;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "en";
        }
        return language.toLowerCase(Locale.ROOT);
    }

    private String textOrNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "flashcardId=" + flashcardId +
                ", term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                ", definitionAr='" + definitionAr + '\'' +
                ", definitionFi='" + definitionFi + '\'' +
                ", definitionKo='" + definitionKo + '\'' +
                ", definitionLo='" + definitionLo + '\'' +
                ", definitionVi='" + definitionVi + '\'' +
                ", flashcardSetId=" + (flashcardSet != null ? flashcardSet.getFlashcardSetId() : null) +
                ", userId=" + (user != null ? user.getUserId() : null) +
                '}';
    }
}
