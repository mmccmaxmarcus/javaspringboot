package br.com.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import br.com.springboot.model.Pessoa;
import br.com.springboot.model.Telefone;
import br.com.springboot.repository.PessoaRepository;
import br.com.springboot.repository.ProfissaoRepository;
import br.com.springboot.repository.TelefoneRepository;
import net.sf.jasperreports.engine.JRException;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ReportUtil reportUtil;

	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	private Pessoa pessoa = new Pessoa();

	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome").ascending())));
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;

	}

	@GetMapping("/pessoapagina")
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model,
			@RequestParam("find") String findNome) {
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(findNome, pageable);	
		model.addObject("pessoas", pagePessoa);
		
		if (pessoa!= null) {
			model.addObject("pessoaobj", this.pessoa);
		}else {
			model.addObject("pessoaobj", new Pessoa());
		}
		model.addObject("find", findNome);
		model.setViewName("cadastro/cadastropessoa");
		return model;

	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = { "multipart/form-data" })
	public ModelAndView save(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file)
			throws IOException {

		pessoa.setTelefones(telefoneRepository.listTelefonesPorPessoa(pessoa.getId()));

		if (bindingResult.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
			andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome").ascending())));
			andView.addObject("pessoaobj", pessoa);

			List<String> msgs = new ArrayList<String>();
			bindingResult.getAllErrors().forEach(error -> {
				msgs.add(error.getDefaultMessage());
			});
			andView.addObject("msg", msgs);

			return andView;
		}

		if (file.getSize() > 0) { // Cadastrando novo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
			pessoa.setTipoFileCurriculo(file.getContentType());
		} else {// Senao edita
			if (pessoa.getId() != null && pessoa.getId() > 0) {
				Pessoa fotoPessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				pessoa.setCurriculo(fotoPessoaTemp.getCurriculo());
				pessoa.setTipoFileCurriculo(fotoPessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(fotoPessoaTemp.getNomeFileCurriculo());
			}
		}

		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome").ascending())));
		andView.addObject("profissoes", profissaoRepository.findAll());
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}

	@GetMapping("/downloadCurriculo/{idpessoa}")
	public void downloadPessoa(@PathVariable("idpessoa") Long idPessoa, HttpServletResponse response)
			throws IOException {
		Pessoa pessoa = pessoaRepository.findById(idPessoa).get();
		if (pessoa.getCurriculo() != null) {
			// Seta o tamanho da resposta
			response.setContentLength(pessoa.getCurriculo().length);
			// Seta o tipo de arquivo para download ou generica (application/octet-stream)
			response.setContentType(pessoa.getTipoFileCurriculo());
			String headerKey = "Content-Disposition";
			String headerValule = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValule);

			// Finaliza a resposta passando o arquivo
			response.getOutputStream().write(pessoa.getCurriculo());
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome").ascending())));
		andView.addObject("pessoaobj", new Pessoa());

		return andView;
	}

	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView edit(@PathVariable("idpessoa") Long idPessoa, @PageableDefault(size = 5) Pageable pageable) {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		pessoa = pessoaRepository.findById(idPessoa).get();
		Iterable<Pessoa> pessoas = pessoaRepository.findAll(pageable);

		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("pessoaobj", pessoa);
		andView.addObject("pessoas", pessoas);
		andView.addObject("profissoes", profissaoRepository.findAll());

		return andView;
	}

	@GetMapping("/removerpessoa/{idpessoa}")
	public ModelAndView remove(@PathVariable("idpessoa") Long idPessoa) {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoaobj", new Pessoa());
		pessoaRepository.deleteById(idPessoa);
		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoas);

		return andView;
	}

	@PostMapping("**/pesquisarnome")
	public ModelAndView find(@RequestParam("find") String nome, @RequestParam("pesquisaSexo") String sexo,
			@PageableDefault(size = 5, sort = { "nome", "sexo" }) Pageable pageable) {

		Page<Pessoa> pessoas = (sexo.isEmpty() || sexo == null) && !nome.isEmpty()
				? pessoaRepository.findPessoaByNamePage(nome, pageable)
				: (nome.isEmpty() || nome == null) && !sexo.isEmpty()
						? pessoaRepository.findPessoaBySexoPage(sexo, pageable)
						: sexo.isEmpty() && nome.isEmpty() ? pessoaRepository.findAll(pageable)
								: pessoaRepository.findPessoaByNameAndSexoPage(nome, sexo, pageable);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("find", nome);
		andView.addObject("pessoas", pessoas);
		return andView;
	}

	@GetMapping("**/pesquisarnome")
	public void imprimePDF(@RequestParam("find") String nome, @RequestParam("pesquisaSexo") String sexo,
			HttpServletRequest request, HttpServletResponse response) throws JRException, IOException {

		List<Pessoa> pessoas = (sexo.isEmpty() || sexo == null) && !nome.isEmpty()
				? pessoaRepository.findPessoaPorNome(nome)
				: (nome.isEmpty() || nome == null) && !sexo.isEmpty() ? pessoaRepository.findPessoaPorSexo(sexo)
						: sexo.isEmpty() && nome.isEmpty() ? (List<Pessoa>) pessoaRepository.findAll()
								: pessoaRepository.findPessoaPorSexoAndNome(nome, sexo);
		// Chamar o servico q faz a geracao de relatorio
		byte[] pdf = reportUtil.gerarReletorio(pessoas, "pessoas", request.getServletContext());

		// Tamanho da resposta do navegador
		response.setContentLength(pdf.length);
		// Indica ao navegador o tipo de resposta
		response.setContentType("application/octet-stream");
		// Cabeçalho da resposta
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);

		// Finaliza a resposta
		response.getOutputStream().write(pdf);

	}

	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idPessoa) {
		ModelAndView andView = new ModelAndView("cadastro/telefones");
		Pessoa pessoa = pessoaRepository.findById(idPessoa).get();

		andView.addObject("pessoaobj", pessoa);
		andView.addObject("telefones", pessoa.getTelefones());

		return andView;
	}

	@GetMapping("/deletetelefone/{idtelefone}")
	public ModelAndView deleteTelefone(@PathVariable("idtelefone") Long idTelefone) {

		ModelAndView andView = new ModelAndView("cadastro/telefones");
		Pessoa pessoa = telefoneRepository.findById(idTelefone).get().getPessoa();
		telefoneRepository.deleteById(idTelefone);
		andView.addObject("pessoaobj", pessoa);
		andView.addObject("telefones", telefoneRepository.listTelefonesPorPessoa(pessoa.getId()));

		return andView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/cadastrotelefone/{pessoaid}")
	public ModelAndView addTelefone(@Valid Telefone telefone, BindingResult bindingResult,
			@PathVariable("pessoaid") Long pessoaid) {

		if (bindingResult.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/telefones");
			Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
			andView.addObject("pessoaobj", pessoa);
			andView.addObject("telefones", telefoneRepository.listTelefonesPorPessoa(pessoaid));

			List<String> msgs = new ArrayList<String>();
			bindingResult.getAllErrors().forEach(error -> {
				msgs.add(error.getDefaultMessage());
			});

			andView.addObject("msgs", msgs);

			return andView;

		}

		ModelAndView andView = new ModelAndView("cadastro/telefones");
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();

		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);
		andView.addObject("pessoaobj", pessoa);
		andView.addObject("telefones", telefoneRepository.listTelefonesPorPessoa(pessoaid));

		return andView;

	}
}
