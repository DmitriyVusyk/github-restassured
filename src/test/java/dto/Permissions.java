package dto;

import com.google.gson.annotations.SerializedName;

public class Permissions{

	@SerializedName("pull")
	private boolean pull;

	@SerializedName("admin")
	private boolean admin;

	@SerializedName("push")
	private boolean push;

	public void setPull(boolean pull){
		this.pull = pull;
	}

	public boolean isPull(){
		return pull;
	}

	public void setAdmin(boolean admin){
		this.admin = admin;
	}

	public boolean isAdmin(){
		return admin;
	}

	public void setPush(boolean push){
		this.push = push;
	}

	public boolean isPush(){
		return push;
	}
}