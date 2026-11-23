package br.com.sistemanf.web.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioNFPrincipal implements UserDetails, CredentialsContainer {

	private static final long serialVersionUID = 1L;

	private final String login;
	private String senhaArmazenada;
	private final String tipo;
	private final int idVinculo;

	public UsuarioNFPrincipal(String login, String senhaArmazenada, String tipo, int idVinculo) {
		this.login = login;
		this.senhaArmazenada = senhaArmazenada;
		this.tipo = tipo;
		this.idVinculo = idVinculo;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + tipo));
	}

	@Override
	public String getPassword() {
		return senhaArmazenada;
	}

	@Override
	public String getUsername() {
		return login;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void eraseCredentials() {
		senhaArmazenada = null;
	}

	public String getTipo() {
		return tipo;
	}

	public int getIdVinculo() {
		return idVinculo;
	}
}