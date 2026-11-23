package br.com.sistemanf.web.repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuditoriaAcademicaRepository {

	private final JdbcTemplate jdbcTemplate;

	public AuditoriaAcademicaRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public void registrar(int idProfessor, int idAluno, int idDisciplina, String tipoRegistro, String operacao,
			Object valorAntigo, Object valorNovo) {

		String hashAnterior = jdbcTemplate.queryForObject(
				"SELECT ultimo_hash FROM auditoria_controle WHERE id_controle = 1 FOR UPDATE", String.class);

		LocalDateTime dataHora = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

		String anterior = valorAntigo == null ? "" : valorAntigo.toString();
		String novo = valorNovo.toString();

		String conteudo = String.join("|", String.valueOf(idProfessor), String.valueOf(idAluno),
				String.valueOf(idDisciplina), tipoRegistro, operacao, anterior, novo,
				dataHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), hashAnterior);

		String hashAtual = gerarHash(conteudo);

		jdbcTemplate.update("""
				INSERT INTO auditoria_academica
				(tipo_registro, operacao, id_professor, id_aluno,
				 id_disciplina, valor_anterior, valor_novo,
				 data_hora, hash_anterior, hash_atual)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""", tipoRegistro, operacao, idProfessor, idAluno, idDisciplina, valorAntigo == null ? null : anterior,
				novo, Timestamp.valueOf(dataHora), hashAnterior, hashAtual);

		int linhas = jdbcTemplate.update("""
				UPDATE auditoria_controle
				SET ultimo_hash = ?
				WHERE id_controle = 1
				""", hashAtual);

		if (linhas != 1) {
			throw new IllegalStateException("Não foi possível atualizar o controle da auditoria.");
		}
	}

	public boolean verificarIntegridade() {

		List<Map<String, Object>> registros = jdbcTemplate.queryForList("""
				SELECT id_professor, id_aluno, id_disciplina,
				       tipo_registro, operacao, valor_anterior,
				       valor_novo, data_hora, hash_anterior, hash_atual
				FROM auditoria_academica
				ORDER BY id_auditoria ASC
				""");

		String hashAnteriorEsperado = "0".repeat(64);

		for (Map<String, Object> registro : registros) {

			int idProfessor = ((Number) registro.get("id_professor")).intValue();
			int idAluno = ((Number) registro.get("id_aluno")).intValue();
			int idDisciplina = ((Number) registro.get("id_disciplina")).intValue();

			String tipoRegistro = (String) registro.get("tipo_registro");
			String operacao = (String) registro.get("operacao");
			String valorAntigo = registro.get("valor_anterior") == null ? ""
					: registro.get("valor_anterior").toString();
			String valorNovo = registro.get("valor_novo").toString();

			Object dataHoraValor = registro.get("data_hora");

			LocalDateTime dataHora;

			if (dataHoraValor instanceof Timestamp timestamp) {
				dataHora = timestamp.toLocalDateTime();
			} else if (dataHoraValor instanceof LocalDateTime localDateTime) {
				dataHora = localDateTime;
			} else {
				return false;
			}

			String hashAnterior = (String) registro.get("hash_anterior");
			String hashAtual = (String) registro.get("hash_atual");

			if (!hashAnteriorEsperado.equals(hashAnterior)) {
				return false;
			}

			String conteudo = String.join("|", String.valueOf(idProfessor), String.valueOf(idAluno),
					String.valueOf(idDisciplina), tipoRegistro, operacao, valorAntigo, valorNovo,
					dataHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), hashAnterior);

			String hashCalculado = gerarHash(conteudo);

			if (!hashCalculado.equals(hashAtual)) {
				return false;
			}

			hashAnteriorEsperado = hashAtual;
		}

		String ultimoHashControle = jdbcTemplate
				.queryForObject("SELECT ultimo_hash FROM auditoria_controle WHERE id_controle = 1", String.class);

		return hashAnteriorEsperado.equals(ultimoHashControle);
	}

	private String gerarHash(String conteudo) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(conteudo.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Não foi possível gerar o hash SHA-256.", e);
		}
	}
}