package com.topzi.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created on 5/7/18.
 */

public class BlocksData {

    @SerializedName("STATUS")
    @Expose
    private String sTATUS;
    @SerializedName("MSG")
    @Expose
    private String mSG;
    @SerializedName("RESULT")
    @Expose
    private RESULT rESULT;

    public String getSTATUS() {
        return sTATUS;
    }

    public void setSTATUS(String sTATUS) {
        this.sTATUS = sTATUS;
    }

    public String getMSG() {
        return mSG;
    }

    public void setMSG(String mSG) {
        this.mSG = mSG;
    }

    public RESULT getRESULT() {
        return rESULT;
    }

    public void setRESULT(RESULT rESULT) {
        this.rESULT = rESULT;
    }


    public class BlockedByMe {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("userId")
        @Expose
        private String userId;
        @SerializedName("friendId")
        @Expose
        private Integer friendId;
        @SerializedName("roomId")
        @Expose
        private String roomId;
        @SerializedName("block")
        @Expose
        private Integer block;
        @SerializedName("createdAt")
        @Expose
        private String createdAt;
        @SerializedName("updatedAt")
        @Expose
        private String updatedAt;
        @SerializedName("deletedAt")
        @Expose
        private Object deletedAt;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Integer getFriendId() {
            return friendId;
        }

        public void setFriendId(Integer friendId) {
            this.friendId = friendId;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public Integer getBlock() {
            return block;
        }

        public void setBlock(Integer block) {
            this.block = block;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Object getDeletedAt() {
            return deletedAt;
        }

        public void setDeletedAt(Object deletedAt) {
            this.deletedAt = deletedAt;
        }

    }

    public class BlockedMe {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("userId")
        @Expose
        private String userId;
        @SerializedName("friendId")
        @Expose
        private Integer friendId;
        @SerializedName("roomId")
        @Expose
        private String roomId;
        @SerializedName("block")
        @Expose
        private Integer block;
        @SerializedName("createdAt")
        @Expose
        private String createdAt;
        @SerializedName("updatedAt")
        @Expose
        private String updatedAt;
        @SerializedName("deletedAt")
        @Expose
        private Object deletedAt;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Integer getFriendId() {
            return friendId;
        }

        public void setFriendId(Integer friendId) {
            this.friendId = friendId;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public Integer getBlock() {
            return block;
        }

        public void setBlock(Integer block) {
            this.block = block;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Object getDeletedAt() {
            return deletedAt;
        }

        public void setDeletedAt(Object deletedAt) {
            this.deletedAt = deletedAt;
        }

    }


    public class RESULT {

        @SerializedName("blockedByMe")
        @Expose
        private List<BlockedByMe> blockedByMe = null;
        @SerializedName("blockedMe")
        @Expose
        private List<BlockedMe> blockedMe = null;

        public List<BlockedByMe> getBlockedByMe() {
            return blockedByMe;
        }

        public void setBlockedByMe(List<BlockedByMe> blockedByMe) {
            this.blockedByMe = blockedByMe;
        }

        public List<BlockedMe> getBlockedMe() {
            return blockedMe;
        }

        public void setBlockedMe(List<BlockedMe> blockedMe) {
            this.blockedMe = blockedMe;
        }

    }
}