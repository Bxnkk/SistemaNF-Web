package br.com.sistemanf.web.repository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HistoricoAlteracoesRepository {

	private final JdbcTemplate jdbcTemplate;

	public HistoricoAlteracoesRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public void registrar(String tipoRegistro, int idRegistro, String acao, Object valorAntigo, Object valorNovo,
			int idProfessor) {

		List<String> hashes = jdbcTemplate.query("""
				SELECT hash_atual
				FROM historico_alteracoes
				ORDER BY id_historico DESC
				LIMIT 1
				FOR UPDATE
				""", (resultado, linha) -> resultado.getString("hash_atual"));

		String hashAnterior = hashes.isEmpty() ? "0".repeat(64) : hashes.get(0);

		boolean nota = "NOTA".equals(tipoRegistro);

		String nomeCampo = nota ? "Nota" : "Faltas";

		String novo = formatarValor(valorNovo);

		String descricao;

		if ("CADASTRO".equals(acao)) {
			descricao = nomeCampo + " cadastrada: " + novo;
		} else {
			String anterior = formatarValor(valorAntigo);
			descricao = nomeCampo + " alterada: " + anterior + " -> " + novo;
		}

		descricao += " | Professor ID: " + idProfessor;

		LocalDateTime dataRegistro = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		String conteudo = String.join("|", tipoRegistro, String.valueOf(idRegistro), acao, descricao,
				dataRegistro.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), hashAnterior);

		String hashAtual = gerarHash(conteudo);

		int linhas = jdbcTemplate.update("""
				INSERT INTO historico_alteracoes
				(
				    tipo_registro,
				    id_registro,
				    acao,
				    descricao,
				    data_registro,
				    hash_anterior,
				    hash_atual
				)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""", tipoRegistro, idRegistro, acao, descricao, Timestamp.valueOf(dataRegistro), hashAnterior,
				hashAtual);

		if (linhas != 1) {
			throw new IllegalStateException("Não foi possível registrar o histórico de alterações.");
		}
	}

	private String formatarValor(Object valor) {
		if (valor instanceof BigDecimal decimal) {
			return decimal.toPlainString();
		}

		return String.valueOf(valor);
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