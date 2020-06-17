package br.com.springboot.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.springboot.model.Telefone;

@Repository
@Transactional
public interface TelefoneRepository extends CrudRepository<Telefone, Long> {
	@Query("select t from telefone t where t.pessoa.id = :param")
	List<Telefone> listTelefonesPorPessoa(@Param("param") Long idPessoa);
}
