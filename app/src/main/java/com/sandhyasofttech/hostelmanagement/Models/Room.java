package com.sandhyasofttech.hostelmanagement.Models;

public class Room {
    public String roomId;
    public String roomNo;
    public String type; // "ROOM" or "DORMITORY"
    public int capacity;
    public int occupied;
    public int available;
    public long createdAt;

    public Room() {}

    public Room(String roomId, String roomNo, String type, int capacity, int occupied, int available, long createdAt) {
        this.roomId = roomId;
        this.roomNo = roomNo;
        this.type = type;
        this.capacity = capacity;
        this.occupied = occupied;
        this.available = available;
        this.createdAt = createdAt;
    }
}
