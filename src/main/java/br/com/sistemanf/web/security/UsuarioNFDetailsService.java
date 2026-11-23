package br.com.sistemanf.web.security;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioNFDetailsService implements UserDetailsService {

	private final JdbcTemplate jdbcTemplate;

	public UsuarioNFDetailsService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public UserDetails loadUserByUsername(String login) {
		if (login == null || login.isBlank()) {
			throw new UsernameNotFoundException("Credenciais inválidas.");
		}

		String sql = """
				SELECT login, tipo, id_professor, id_aluno,
				       senha_hash, senha_salt
				FROM usuario
				WHERE login = ?
				""";

		List<UsuarioNFPrincipal> usuarios = jdbcTemplate.query(sql, (rs, linha) -> {
			String tipo = rs.getString("tipo");
			int idVinculo;

			if ("PROFESSOR".equals(tipo)) {
				idVinculo = rs.getInt("id_professor");
			} else if ("ALUNO".equals(tipo)) {
				idVinculo = rs.getInt("id_aluno");
			} else {
				throw new UsernameNotFoundException("Credenciais inválidas.");
			}

			boolean vinculoNulo = rs.wasNull();

			if (idVinculo <= 0 || vinculoNulo) {
				throw new UsernameNotFoundException("Credenciais inválidas.");
			}

			String salt = rs.getString("senha_salt");
			String hash = rs.getString("senha_hash");

			if (salt == null || hash == null || salt.isBlank() || hash.isBlank()) {
				throw new UsernameNotFoundException("Credenciais inválidas.");
			}

			return new UsuarioNFPrincipal(rs.getString("login"), salt + ":" + hash, tipo, idVinculo);
		}, login.trim());

		if (usuarios.isEmpty()) {
			throw new UsernameNotFoundException("Credenciais inválidas.");
		}

		return usuarios.get(0);
	}
}