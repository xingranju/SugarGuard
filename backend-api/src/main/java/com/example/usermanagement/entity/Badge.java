package com.example.usermanagement.entity;

import javax.persistence.*;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(length = 20)
    private String category;

    @Column(name = "condition_desc", length = 200)
    private String conditionDesc;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Badge() {}

    public Badge(String name, String description, String iconName, String category,
                 String conditionDesc, Integer threshold, Integer sortOrder) {
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.category = category;
        this.conditionDesc = conditionDesc;
        this.threshold = threshold;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getConditionDesc() { return conditionDesc; }
    public void setConditionDesc(String conditionDesc) { this.conditionDesc = conditionDesc; }
    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
