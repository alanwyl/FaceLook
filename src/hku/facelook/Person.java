package hku.facelook;

public class Person {
	private int id;
	private String name, mobile, email;
	private String vector;
	
	public Person(int id, String vector){
		this.id = id;
		this.vector = vector;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public void setInfo(String name, String mobile, String email){
		this.name = name;
		this.mobile = mobile;
		this.email = email;
	}
	
	public void setVector(String vector){
		this.vector = vector;
	}
	
	public int getId(){
		return id;
	}
	
	public String getVector(){
		return vector;
	}
	
}
