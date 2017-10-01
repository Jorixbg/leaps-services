package com.leaps.model.token;

import java.util.Objects;

public class Token {
	private Long id;
	private Long lastModified;
	
	Token(Long id) {
		setId(id);
		setLastModified(System.currentTimeMillis());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

    @Override
    public boolean equals(Object o) {
        if (o == this) {
        	return true;
        }
        if (!(o instanceof Token)) {
            return false;
        }
        
        Token token = (Token) o;
        return id == token.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastModified);
    }
}
