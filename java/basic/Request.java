package basic;

public class Request {
    String objectName;
    int count;
    String containerName;
    public Request(){

    }
    public Request(String objectName){
        this.objectName = objectName;
    }
    public Request(int count){
        this.count = count;
    }
    public Request(String objectName, int count){
        this.objectName = objectName;
        this.count = count;
    }
    public Request(String objectName, String containerName, int count){
        this.objectName=objectName;
        this.containerName = containerName;
        this.count = count;
    }
    // setter
    public void setObjectName(String objectName){
        this.objectName = objectName;
    }
    public void setCount(int count){
        this.count=count;
    }
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public int getCount() {
        return this.count;
    }

    public String getObjectName() {
        return this.objectName;
    }
    public String getContainerName(){return this.containerName;}
}
