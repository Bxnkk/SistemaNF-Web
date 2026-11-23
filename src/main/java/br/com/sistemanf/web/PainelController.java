package br.com.sistemanf.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemanf.web.model.RegistroAcademico;
import br.com.sistemanf.web.repository.AuditoriaAcademicaRepository;
import br.com.sistemanf.web.repository.RegistroAcademicoRepository;
import br.com.sistemanf.web.security.UsuarioNFPrincipal;

@Controller
public class PainelController {

	private static final Logger logger = LoggerFactory.getLogger(PainelController.class);

	private final RegistroAcademicoRepository registroRepository;
	private final AuditoriaAcademicaRepository auditoriaRepository;

	public PainelController(RegistroAcademicoRepository registroRepository,
			AuditoriaAcademicaRepository auditoriaRepository) {
		this.registroRepository = registroRepository;
		this.auditoriaRepository = auditoriaRepository;
	}

	@GetMapping("/professor")
	public String painelProfessor(@RequestParam(required = false) String busca, Authentication authentication,
			Model model) {
		UsuarioNFPrincipal usuario = (UsuarioNFPrincipal) authentication.getPrincipal();
		List<RegistroAcademico> registros = registroRepository.buscarAlunosDoProfessor(usuario.getIdVinculo(), busca);

		model.addAttribute("usuario", usuario.getUsername());
		model.addAttribute("registros", registros);
		model.addAttribute("busca", busca == null ? "" : busca);

		return "professor";
	}

	@PostMapping("/professor/auditoria/verificar")
	public String verificarAuditoria(@RequestParam(required = false) String busca,
			RedirectAttributes redirectAttributes) {
		try {
			boolean integra = auditoriaRepository.verificarIntegridade();

			redirectAttributes.addFlashAttribute("auditoriaIntegra", integra);

			if (integra) {
				redirectAttributes.addFlashAttribute("auditoriaMensagem",
						"A cadeia de auditoria está íntegra. Nenhuma inconsistência foi encontrada.");
			} else {
				redirectAttributes.addFlashAttribute("auditoriaMensagem",
						"Atenção! Foram encontradas inconsistências na cadeia de auditoria.");
			}
		} catch (DataAccessException e) {
			logger.error("Erro ao verificar a integridade da auditoria.", e);
			redirectAttributes.addFlashAttribute("auditoriaErro",
					"Não foi possível verificar a auditoria. Confira a conexão com o banco de dados.");
		}

		return redirecionarProfessor(busca, redirectAttributes);
	}

	@PostMapping("/professor/nota")
	public String salvarNota(@RequestParam("matricula") String matricula, @RequestParam("nota") String entrada,
			@RequestParam(required = false) String busca, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		UsuarioNFPrincipal usuario = (UsuarioNFPrincipal) authentication.getPrincipal();

		try {
			String resultado = registroRepository.salvarNota(usuario.getIdVinculo(), matricula, entrada);
			redirectAttributes.addFlashAttribute("mensagem", resultado);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("erro", e.getMessage());
		} catch (DataAccessException e) {
			logger.error("Erro ao salvar nota do aluno com matrícula {}.", matricula, e);
			redirectAttributes.addFlashAttribute("erro",
					"Não foi possível salvar a nota. Verifique a conexão e as permissões do banco de dados.");
		}

		return redirecionarProfessor(busca, redirectAttributes);
	}

	@PostMapping("/professor/faltas")
	public String salvarFaltas(@RequestParam("matricula") String matricula, @RequestParam("faltas") String entrada,
			@RequestParam(required = false) String busca, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		UsuarioNFPrincipal usuario = (UsuarioNFPrincipal) authentication.getPrincipal();

		try {
			String resultado = registroRepository.salvarFaltas(usuario.getIdVinculo(), matricula, entrada);
			redirectAttributes.addFlashAttribute("mensagem", resultado);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("erro", e.getMessage());
		} catch (DataAccessException e) {
			logger.error("Erro ao salvar faltas do aluno com matrícula {}.", matricula, e);
			redirectAttributes.addFlashAttribute("erro", "Ocorreu um erro ao salvar as faltas. Tente novamente.");
		}

		return redirecionarProfessor(busca, redirectAttributes);
	}

	@GetMapping("/aluno")
	public String painelAluno(Authentication authentication, Model model) {
		UsuarioNFPrincipal usuario = (UsuarioNFPrincipal) authentication.getPrincipal();
		List<RegistroAcademico> registros = registroRepository.buscarDadosDoAluno(usuario.getIdVinculo());

		model.addAttribute("usuario", usuario.getUsername());
		model.addAttribute("registros", registros);

		return "aluno";
	}

	private String redirecionarProfessor(String busca, RedirectAttributes redirectAttributes) {
		if (busca != null && !busca.isBlank()) {
			redirectAttributes.addAttribute("busca", busca);
		}

		return "redirect:/professor";
	}
}