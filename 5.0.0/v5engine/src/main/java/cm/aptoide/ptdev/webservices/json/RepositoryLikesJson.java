package cm.aptoide.ptdev.webservices.json;

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Created by rmateus on 18-02-2014.
 */
public class RepositoryLikesJson{
    @Key private List<Listing> listing;
    @Key private String status;

    public List<Listing> getListing(){
        return this.listing;
    }

    public String getStatus(){
        return this.status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public static class Listing{
        @Key private String apkid;
        @Key private String like;
        @Key private String name;
        @Key private String useridhash;
        @Key private String username;
        @Key private String ver;
        @Key private Number vercode;

        public String getApkid(){
            return this.apkid;
        }
        public void setApkid(String apkid){
            this.apkid = apkid;
        }
        public String getLike(){
            return this.like;
        }
        public void setLike(String like){
            this.like = like;
        }
        public String getName(){
            return this.name;
        }
        public void setName(String name){
            this.name = name;
        }
        public String getUseridhash(){
            return this.useridhash;
        }
        public void setUseridhash(String useridhash){
            this.useridhash = useridhash;
        }
        public String getUsername(){
            return this.username;
        }
        public void setUsername(String username){
            this.username = username;
        }
        public String getVer(){
            return this.ver;
        }
        public void setVer(String ver){
            this.ver = ver;
        }
        public Number getVercode(){
            return this.vercode;
        }
        public void setVercode(Number vercode){
            this.vercode = vercode;
        }
    }
}
