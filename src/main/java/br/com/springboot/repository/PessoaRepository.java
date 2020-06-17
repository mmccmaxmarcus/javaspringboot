package br.com.springboot.repository;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.springboot.model.Pessoa;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

	@Query("select p from pessoa as p where p.nome like %:param%")
	List<Pessoa> findPessoaPorNome(@Param("param") String nome);

	@Query("select p from pessoa as p where p.nome like %:paramNome% and p.sexo = :paramSexo")
	List<Pessoa> findPessoaPorSexoAndNome(@Param("paramNome") String nome, @Param("paramSexo") String sexo);

	@Query("select p from pessoa as p where p.sexo = :paramSexo")
	List<Pessoa> findPessoaPorSexo(@Param("paramSexo") String sexo);

	default Page<Pessoa> findPessoaByNamePage(String nome, Pageable pageable) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		// Estamos configurando a pesquisa para consultar por partes do nome no banco de
		// dados, igual ao like do sql
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher("nome",
				ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

		// Une o objeto e o valor para consultar
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		Page<Pessoa> pessoas = findAll(example, pageable);

		return pessoas;

	}

	default Page<Pessoa> findPessoaBySexoPage(String sexo, Pageable pageable) {
		Pessoa pessoa = new Pessoa();
		pessoa.setSexo(sexo);

		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher("sexo",
				ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		Page<Pessoa> pessoas = findAll(example, pageable);

		return pessoas;
	}
	
	default Page<Pessoa> findPessoaByNameAndSexoPage(String nome, String sexo, Pageable pageable) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		pessoa.setSexo(sexo);
		// Estamos configurando a pesquisa para consultar por partes do nome no banco de
		// dados, igual ao like do sql
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny().withMatcher("nome",
				ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
				.withMatcher("sexo", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

		// Une o objeto e o valor para consultar
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);

		Page<Pessoa> pessoas = findAll(example, pageable);

		return pessoas;

	}

}
