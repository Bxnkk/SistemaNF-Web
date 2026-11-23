package br.com.sistemanf.web.model;

import java.math.BigDecimal;

public class RegistroAcademico {

	private final String matricula;
	private final String nome;
	private final String turma;
	private final String disciplina;
	private final BigDecimal nota;
	private final int faltas;
	private final double frequencia;
	private final String situacao;

	public RegistroAcademico(String matricula, String nome, String turma, String disciplina, BigDecimal nota,
			int faltas, double frequencia, String situacao) {

		this.matricula = matricula;
		this.nome = nome;
		this.turma = turma;
		this.disciplina = disciplina;
		this.nota = nota;
		this.faltas = faltas;
		this.frequencia = frequencia;
		this.situacao = situacao;
	}

	public String getMatricula() {
		return matricula;
	}

	public String getNome() {
		return nome;
	}

	public String getTurma() {
		return turma;
	}

	public String getDisciplina() {
		return disciplina;
	}

	public BigDecimal getNota() {
		return nota;
	}

	public int getFaltas() {
		return faltas;
	}

	public double getFrequencia() {
		return frequencia;
	}

	public String getSituacao() {
		return situacao;
	}
}