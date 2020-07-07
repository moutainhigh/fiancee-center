package com.njwd.exception;

import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.authc.AuthenticationException;

@Getter
@Setter
public class UserException extends AuthenticationException {
	private static final long serialVersionUID = -6119898068501525446L;
	private ResultCode resultCode;

	public UserException(ResultCode resultCode) {
		this.resultCode = resultCode;
	}
}
