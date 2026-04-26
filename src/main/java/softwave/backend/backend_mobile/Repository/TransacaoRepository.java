package softwave.backend.backend_mobile.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import softwave.backend.backend_mobile.Entity.TransacaoEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<TransacaoEntity, Integer>, JpaSpecificationExecutor<TransacaoEntity> {

    List<TransacaoEntity> findByHonorario_Processo_IdIn(Collection<Integer> processoIds);

    List<TransacaoEntity> findByHonorario_IdOrderByDataEmissaoAsc(Integer honorarioId);
    List<TransacaoEntity> findByDataVencimentoBeforeAndDataPagamentoIsNull(LocalDate data);

    @Query("""
            select t from TransacaoEntity t
            join fetch t.honorario h
            join fetch h.processo p
            where p.id in :processoIds
            and t.dataEmissao between :ini and :fim
            """)
    List<TransacaoEntity> findByProcessoInAndDataEmissaoBetween(
            @Param("processoIds") Collection<Integer> processoIds,
            @Param("ini") LocalDate ini,
            @Param("fim") LocalDate fim
    );

    long countByHonorario_Processo_IdInAndStatusAprovacaoIgnoreCase(
            Collection<Integer> processoIds,
            String statusAprovacao
    );

    @Query("""
            select coalesce(sum(t.valor), 0) from TransacaoEntity t
            join t.honorario h
            where h.processo.id in :processoIds
            and lower(coalesce(t.tipo,'')) like '%receita%'
            and t.dataEmissao between :ini and :fim
            """)
    BigDecimal sumReceitaByProcessosAndPeriod(
            @Param("processoIds") Collection<Integer> processoIds,
            @Param("ini") LocalDate ini,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select coalesce(sum(t.valor), 0) from TransacaoEntity t
            join t.honorario h
            where h.processo.id in :processoIds
            and lower(coalesce(t.tipo,'')) like '%despesa%'
            and t.dataEmissao between :ini and :fim
            """)
    BigDecimal sumDespesaByProcessosAndPeriod(
            @Param("processoIds") Collection<Integer> processoIds,
            @Param("ini") LocalDate ini,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select count(t) from TransacaoEntity t
            join t.honorario h
            where h.processo.id in :processoIds
            and t.dataEmissao between :ini and :fim
            """)
    long countByProcessosAndPeriod(
            @Param("processoIds") Collection<Integer> processoIds,
            @Param("ini") LocalDate ini,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select u.id, u.nome, sum(t.valor)
            from TransacaoEntity t
            join t.honorario h, UsuarioProcessoEntity up, UsuarioEntity u
            where h.processo.id = up.processo.id
            and u.id = up.usuario.id
            and h.processo.id in :processoIds
            and u.tipoUsuario like '%usuario_fisico%'
            and lower(coalesce(t.tipo,'')) like '%receita%'
            and t.dataEmissao between :ini and :fim
            group by u.id, u.nome
            order by sum(t.valor) desc
            """)
    List<Object[]> rankingReceita(
            @Param("processoIds") Collection<Integer> processoIds,
            @Param("ini") LocalDate ini,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );
}
