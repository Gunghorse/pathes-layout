package com.github.gunghorse.questCreator.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.gunghorse.questCreator.Keys;
import com.github.gunghorse.questCreator.quests.Quest;
import com.github.gunghorse.questCreator.quests.points.QuestPoint;
import com.github.gunghorse.questCreator.repositories.QuestRepository;
import org.neo4j.ogm.annotation.*;

import java.util.*;

import static org.neo4j.ogm.annotation.Relationship.OUTGOING;

@NodeEntity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Index(unique = true)
    private String username;

    @JsonIgnore
    private String password;

    @Index(unique = true)
    private String email;

    @JsonIgnoreProperties({"user","players","creator"})
    @Relationship(type = Keys.PLAYING)
    private List<Quest> playing = new ArrayList<>();

    @JsonIgnoreProperties({"user","players","creator"})
    @Relationship(type = Keys.CREATED_BY, direction = OUTGOING)
    private List<Quest> createdQuests = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void addCreatedQuest(Quest quest){
        createdQuests.add(quest);
    }

    public void startQuestSession(Quest quest){
        playing.add(quest);
        quest.addPlayer(this);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Quest> getCreatedQuests() {
        return createdQuests;
    }

    public void setCreatedQuests(List<Quest> createdQuests) {
        this.createdQuests = createdQuests;
    }


    private HashSet<Quest> completedQuestsId = new HashSet<>();
    private HashMap<Quest, HashSet<QuestPoint>> activeQuestsVisitedPoints = new HashMap<>();
    private HashMap<Quest, HashSet<QuestPoint>> activeQuestsVisiblePoints = new HashMap<>();


    public boolean addActiveQuest(Quest quest){
        if (quest == null) return false;
        HashSet<QuestPoint> points = new HashSet<>();
        points.add(quest.getStartPoint());
        activeQuestsVisiblePoints.put(quest, points);
        activeQuestsVisitedPoints.put(quest, new HashSet<>());
        return true;
    }

    public boolean visitPoint(Quest quest, QuestPoint point){
        if (quest == null || point == null) return false;

        HashSet<QuestPoint> visible = activeQuestsVisiblePoints.get(quest);
        visible.remove(point);
        visible.addAll(point.getChildren());

        HashSet<QuestPoint> visited = activeQuestsVisitedPoints.get(quest);
        visited.add(point);

        if (visited.size() == quest.getPoints().size()){    // if quest completed
            completedQuestsId.add(quest);
            activeQuestsVisitedPoints.remove(quest);
            activeQuestsVisiblePoints.remove(quest);
        }

        return true;
    }



}
