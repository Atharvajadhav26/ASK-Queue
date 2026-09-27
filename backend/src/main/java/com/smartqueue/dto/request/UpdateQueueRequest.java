package com.smartqueue.dto.request;

public class UpdateQueueRequest {
    private String name;
    private String serviceType;
    private String description;
    private Integer maxCapacity;
    private Integer avgTimePerPerson;
    private Integer holdTimeMinutes;
    private String workingHoursStart;
    private String workingHoursEnd;

    public UpdateQueueRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Integer getAvgTimePerPerson() { return avgTimePerPerson; }
    public void setAvgTimePerPerson(Integer avgTimePerPerson) { this.avgTimePerPerson = avgTimePerPerson; }
    public Integer getHoldTimeMinutes() { return holdTimeMinutes; }
    public void setHoldTimeMinutes(Integer holdTimeMinutes) { this.holdTimeMinutes = holdTimeMinutes; }
    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }
    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }
}
