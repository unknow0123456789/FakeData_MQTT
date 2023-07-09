package com.example.fakedataoffical;

public class JsonPropertyMinimal {
    public String NAME;
    public String VALUE;
    public Double ChangeRate;
    public JsonPropertyMinimal(String name, String value,Double changerate)
    {
        this.NAME=name;
        this.VALUE=value;
        this.ChangeRate=changerate;
    }
    public JsonPropertyMinimal CreateDeepClone()
    {
        return new JsonPropertyMinimal(this.NAME,this.VALUE,this.ChangeRate);
    }
}
