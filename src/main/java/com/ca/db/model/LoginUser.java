package com.ca.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LoginUser")
public class LoginUser {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "dflag")
	private int dFlag;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "invalid_count")
	private int invalid_count;

	@Column(name = "last_login_date")
	private Date lastLoginDate;

	@Column(name = "lastmodifieddate")
	private Date lastModifiedDate;

	public int getdFlag() {
		return this.dFlag;
	}

	public void setdFlag(int dFlag) {
		this.dFlag = dFlag;
	}

	public Date getLastModifiedDate() {
		return this.lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getInvalid_count() {
		return this.invalid_count;
	}

	public void setInvalid_count(int invalid_count) {
		this.invalid_count = invalid_count;
	}

	public Date getLastLoginDate() {
		return this.lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = 31 * result + this.id;
		result = 31 * result + (this.password == null ? 0 : this.password.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoginUser other = (LoginUser) obj;
		if (this.id != other.id)
			return false;
		if (this.password == null) {
			if (other.password != null)
				return false;
		} else if (!this.password.equals(other.password))
			return false;
		return true;
	}
}