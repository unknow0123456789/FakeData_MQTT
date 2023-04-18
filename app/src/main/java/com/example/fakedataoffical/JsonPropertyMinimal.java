package com.example.fakedataoffical;

public class JsonPropertyMinimal {
    public String NAME;
    public String VALUE;
    public JsonPropertyMinimal(String name, String value)
    {
        this.NAME=name;
        this.VALUE=value;
    }
    public JsonPropertyMinimal CreateDeepClone()
    {
        return new JsonPropertyMinimal(this.NAME,this.VALUE);
    }
}
