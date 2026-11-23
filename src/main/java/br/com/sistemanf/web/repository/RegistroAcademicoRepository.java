package br.com.sistemanf.web.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemanf.web.model.RegistroAcademico;

@Repository
public class RegistroAcademicoRepository {

	private static final BigDecimal NOTA_MINIMA = new BigDecimal("6.00");

	private static final String CONSULTA_BASE = """
			SELECT a.matricula, a.nome,
			       t.nome AS turma,
			       d.nome AS disciplina,
			       (SELECT n.valor
			        FROM nota n
			        WHERE n.id_aluno = a.id_aluno
			        AND n.id_disciplina = t.id_disciplina
			        ORDER BY n.id_nota DESC
			        LIMIT 1) AS nota,
			       COALESCE(
			           (SELECT f.quantidade
			            FROM falta f
			            WHERE f.id_aluno = a.id_aluno
			            AND f.id_disciplina = t.id_disciplina
			            ORDER BY f.id_falta DESC
			            LIMIT 1), 0
			       ) AS faltas
			FROM aluno a
			JOIN turma t ON a.id_turma = t.id_turma
			JOIN disciplina d ON t.id_disciplina = d.id_disciplina
			""";

	private final JdbcTemplate jdbcTemplate;
	private final AuditoriaAcademicaRepository auditoriaRepository;
	private final HistoricoAlteracoesRepository historicoRepository;

	public RegistroAcademicoRepository(JdbcTemplate jdbcTemplate, AuditoriaAcademicaRepository auditoriaRepository,
			HistoricoAlteracoesRepository historicoRepository) {

		this.jdbcTemplate = jdbcTemplate;
		this.auditoriaRepository = auditoriaRepository;
		this.historicoRepository = historicoRepository;
	}

	public List<RegistroAcademico> buscarAlunosDoProfessor(int idProfessor, String busca) {

		StringBuilder sql = new StringBuilder(CONSULTA_BASE);
		List<Object> parametros = new ArrayList<>();

		sql.append(" WHERE t.id_professor = ? ");
		parametros.add(idProfessor);

		if (busca != null && !busca.isBlank()) {
			sql.append(" AND (a.nome LIKE ? OR a.matricula LIKE ?) ");

			String filtro = "%" + busca.trim() + "%";

			parametros.add(filtro);
			parametros.add(filtro);
		}

		sql.append(" ORDER BY a.nome");

		return jdbcTemplate.query(sql.toString(), (resultado, linha) -> mapearRegistro(resultado),
				parametros.toArray());
	}

	public List<RegistroAcademico> buscarDadosDoAluno(int idAluno) {

		String sql = CONSULTA_BASE + " WHERE a.id_aluno = ? ORDER BY a.nome";

		return jdbcTemplate.query(sql, (resultado, linha) -> mapearRegistro(resultado), idAluno);
	}

	@Transactional
	public String salvarNota(int idProfessor, String matricula, String entrada) {

		BigDecimal nota;

		try {
			nota = new BigDecimal(entrada.trim().replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
		} catch (NumberFormatException | ArithmeticException e) {
			throw new IllegalArgumentException("Informe uma nota válida, com até duas casas decimais.");
		}

		if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
			throw new IllegalArgumentException("A nota precisa estar entre 0 e 10.");
		}

		return salvarRegistro(idProfessor, matricula, nota, true);
	}

	@Transactional
	public String salvarFaltas(int idProfessor, String matricula, String entrada) {

		int faltas;

		try {
			faltas = Integer.parseInt(entrada.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Informe uma quantidade inteira de faltas.");
		}

		if (faltas < 0 || faltas > 120) {
			throw new IllegalArgumentException("As faltas devem estar entre 0 e 120 aulas.");
		}

		return salvarRegistro(idProfessor, matricula, faltas, false);
	}

	private String salvarRegistro(int idProfessor, String matricula, Object novoValor, boolean nota) {

		if (idProfessor <= 0) {
			throw new SecurityException("Professor não identificado.");
		}

		if (matricula == null || matricula.isBlank()) {
			throw new IllegalArgumentException("Informe a matrícula do aluno.");
		}

		List<Map<String, Object>> alunos = jdbcTemplate.queryForList("""
				SELECT a.id_aluno, a.nome, t.id_disciplina
				FROM aluno a
				JOIN turma t ON t.id_turma = a.id_turma
				WHERE a.matricula = ? AND t.id_professor = ?
				""", matricula.trim(), idProfessor);

		if (alunos.isEmpty()) {
			return "Matrícula não encontrada ou acesso não permitido.";
		}

		Map<String, Object> aluno = alunos.get(0);

		int idAluno = ((Number) aluno.get("id_aluno")).intValue();
		int idDisciplina = ((Number) aluno.get("id_disciplina")).intValue();
		String nome = (String) aluno.get("nome");

		String tabela = nota ? "nota" : "falta";
		String idCampo = nota ? "id_nota" : "id_falta";
		String valorCampo = nota ? "valor" : "quantidade";

		List<Map<String, Object>> registros = jdbcTemplate.queryForList("SELECT " + idCampo + " AS id_registro, "
				+ valorCampo + " AS valor_antigo FROM " + tabela + " WHERE id_aluno = ? AND id_disciplina = ?"
				+ " ORDER BY " + idCampo + " DESC LIMIT 1 FOR UPDATE", idAluno, idDisciplina);

		boolean cadastro = registros.isEmpty();
		Integer idRegistro = null;
		Object valorAntigo = null;

		if (!cadastro) {
			Map<String, Object> registro = registros.get(0);
			idRegistro = ((Number) registro.get("id_registro")).intValue();
			valorAntigo = registro.get("valor_antigo");

			boolean mesmoValor;

			if (nota) {
				mesmoValor = ((BigDecimal) valorAntigo).compareTo((BigDecimal) novoValor) == 0;
			} else {
				mesmoValor = ((Number) valorAntigo).intValue() == ((Integer) novoValor).intValue();
			}

			if (mesmoValor) {
				return "Esse valor já está cadastrado para " + nome + ".";
			}
		}

		if (cadastro) {
			KeyHolder chave = new GeneratedKeyHolder();

			PreparedStatementCreator comando = conexao -> {
				PreparedStatement ps = conexao.prepareStatement("INSERT INTO " + tabela + " (" + valorCampo
						+ ", id_aluno, id_disciplina)" + " VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

				if (nota) {
					ps.setBigDecimal(1, (BigDecimal) novoValor);
				} else {
					ps.setInt(1, (Integer) novoValor);
				}

				ps.setInt(2, idAluno);
				ps.setInt(3, idDisciplina);

				return ps;
			};

			jdbcTemplate.update(comando, chave);

			Number idGerado = chave.getKey();

			if (idGerado == null) {
				throw new IllegalStateException("Não foi possível obter o ID do registro criado.");
			}

			idRegistro = idGerado.intValue();

		} else {

			int linhas;

			if (nota) {
				linhas = jdbcTemplate.update(
						"UPDATE nota SET valor = ?" + " WHERE id_nota = ? AND id_aluno = ? AND id_disciplina = ?",
						novoValor, idRegistro, idAluno, idDisciplina);
			} else {
				linhas = jdbcTemplate.update(
						"UPDATE falta SET quantidade = ?"
								+ " WHERE id_falta = ? AND id_aluno = ? AND id_disciplina = ?",
						novoValor, idRegistro, idAluno, idDisciplina);
			}

			if (linhas != 1) {
				throw new IllegalStateException("Não foi possível atualizar o registro.");
			}
		}

		String operacao = cadastro ? "CADASTRO" : "ALTERACAO";
		String tipoRegistro = nota ? "NOTA" : "FALTA";

		auditoriaRepository.registrar(idProfessor, idAluno, idDisciplina, tipoRegistro, operacao, valorAntigo,
				novoValor);

		historicoRepository.registrar(tipoRegistro, idRegistro, operacao, valorAntigo, novoValor, idProfessor);

		return (cadastro ? "Cadastro" : "Alteração") + " de " + (nota ? "nota" : "faltas")
				+ " realizado com sucesso para " + nome + ".";
	}

	private RegistroAcademico mapearRegistro(ResultSet resultado) throws SQLException {

		BigDecimal nota = resultado.getBigDecimal("nota");
		int faltas = resultado.getInt("faltas");

		double frequencia = 100.0 * (120 - faltas) / 120;

		String situacao = calcularSituacao(nota, faltas);

		return new RegistroAcademico(resultado.getString("matricula"), resultado.getString("nome"),
				resultado.getString("turma"), resultado.getString("disciplina"), nota, faltas, frequencia, situacao);
	}

	private String calcularSituacao(BigDecimal nota, int faltas) {

		if (nota == null) {
			return "Indefinida";
		}

		boolean notaOk = nota.compareTo(NOTA_MINIMA) >= 0;
		boolean faltasOk = faltas <= 30;

		if (notaOk && faltasOk) {
			return "Aprovado";
		}

		if (!notaOk && !faltasOk) {
			return "Reprovado por nota e faltas";
		}

		return notaOk ? "Reprovado por faltas" : "Reprovado por nota";
	}
}