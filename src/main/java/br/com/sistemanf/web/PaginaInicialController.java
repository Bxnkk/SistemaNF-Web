package br.com.sistemanf.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaInicialController {

	@GetMapping("/")
	public String inicio(Authentication authentication) {

		boolean professor = authentication.getAuthorities().stream()
				.anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_PROFESSOR"));

		if (professor) {
			return "redirect:/professor";
		}

		boolean aluno = authentication.getAuthorities().stream()
				.anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_ALUNO"));

		if (aluno) {
			return "redirect:/aluno";
		}

		return "redirect:/login";
	}
}