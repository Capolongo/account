package br.com.account.exception;

import lombok.Getter;

@Getter
public class AccountException extends RuntimeException {

    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountException(String message) {
        super(message);
    }
}

