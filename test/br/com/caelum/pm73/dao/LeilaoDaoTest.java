package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.builder.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Lance;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;
	private Leilao leilao;

	@Before
	public void antes() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		leilaoDao = new LeilaoDao(session);
		leilao = new LeilaoBuilder().constroi();
		session.beginTransaction();
	}
	
	@After
	public void depois() {
		session.getTransaction().rollback();
		session.close();
	}
	
	@Test
	public void deveContarLeiloesNaoEncerrados() {
		Usuario judith = new Usuario("Judith", "judith@email.com");
		
		Leilao ativo = new Leilao("RTX 3080", 5000.00, judith , false);
		Leilao encerrado = new Leilao("RTX 3090", 10000.00, judith, false);
		encerrado.encerra();
		
		usuarioDao.salvar(judith);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);
		
		long total = leilaoDao.total();
		
		assertEquals(1L, total);
	}
	
	@Test
	public void deveRetornarZeroCasoTodosEstejamEncerrados() {
		Usuario judith = new Usuario("Judith", "judith@email.com");
		
		Leilao encerrado1 = new Leilao("RTX 3080", 5000.00, judith , false);
		Leilao encerrado2 = new Leilao("RTX 3090", 10000.00, judith, false);
		encerrado1.encerra();
		encerrado2.encerra();
		
		usuarioDao.salvar(judith);
		leilaoDao.salvar(encerrado1);
		leilaoDao.salvar(encerrado2);
		
		long total = leilaoDao.total();
		
		assertEquals(0L, total);
	}
	
	@Test
	public void deveRetornarApenasNaoUsados() {
		Usuario judith = new Usuario("Judith", "judith@email.com");
		
		Leilao leilaoProdutoAntigo = new Leilao("RTX 3080", 5000.00, judith , true);
		Leilao leilaoProdutoNovo = new Leilao("RTX 3090", 10000.00, judith, false);
		leilaoProdutoAntigo.setUsado(true);
		
		usuarioDao.salvar(judith);
		leilaoDao.salvar(leilaoProdutoAntigo);
		leilaoDao.salvar(leilaoProdutoNovo);

		List<Leilao> leiloesNovos = leilaoDao.novos();
		
		assertEquals(1, leiloesNovos.size());
		assertEquals("RTX 3090", leiloesNovos.get(0).getNome());
	}
	
	@Test
	public void deveRetornarApenasLeiloesDeMaisDeUmaSemana() {
		Calendar estaSemana = Calendar.getInstance();
		Calendar semanaPassada = Calendar.getInstance();
		
		estaSemana.add(Calendar.DAY_OF_MONTH, -2);
		semanaPassada.add(Calendar.DAY_OF_MONTH, -10);
		
		Usuario judith = new Usuario("Judith", "judith@email.com");
		
		Leilao leilaoAntigo1 = new Leilao("RTX 2080 SUPER", 5000.00, judith , false);
		Leilao leilaoAntigo2 = new Leilao("RTX 2080 TI", 10000.00, judith, false);
		
		leilaoAntigo1.setDataAbertura(semanaPassada);
		leilaoAntigo2.setDataAbertura(semanaPassada);
		
		Leilao leilaoNovo1 = new Leilao("RTX 3080", 5000.00, judith , false);
		Leilao leilaoNovo2 = new Leilao("RTX 3090", 10000.00, judith, false);
		
		leilaoNovo1.setDataAbertura(estaSemana);
		leilaoNovo2.setDataAbertura(estaSemana);
		
		usuarioDao.salvar(judith);
		
		leilaoDao.salvar(leilaoAntigo1);
		leilaoDao.salvar(leilaoAntigo2);
		leilaoDao.salvar(leilaoNovo1);
		leilaoDao.salvar(leilaoNovo2);
		
		List<Leilao> leiloesSemanaPassada = leilaoDao.antigos();
		
		assertEquals(2, leiloesSemanaPassada.size());
		assertEquals("RTX 2080 SUPER", leiloesSemanaPassada.get(0).getNome());
		assertEquals("RTX 2080 TI", leiloesSemanaPassada.get(1).getNome());
	}
	
	@Test
	public void deveRetornarLeiloesDeExatosSeteDias() {
		Calendar semanaPassada = Calendar.getInstance();
		semanaPassada.add(Calendar.DAY_OF_MONTH, -7);
		
		Usuario judith = new Usuario("Judith", "judith@email.com");
		
		usuarioDao.salvar(judith);
		
		Leilao leilao = new Leilao("RTX 3080", 5000.00, judith , false);
		
		leilao.setDataAbertura(semanaPassada);
		
		leilaoDao.salvar(leilao);
		
		List<Leilao> leiloes = leilaoDao.porPeriodo(semanaPassada, semanaPassada);
		
		assertEquals(1, leiloes.size());
		assertEquals("RTX 3080", leiloes.get(0).getNome());
	}

	@Test
	public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {
		Calendar inicio = Calendar.getInstance();
		Calendar fim = Calendar.getInstance();
		inicio.add(Calendar.DAY_OF_MONTH, -10);
		
		Usuario judith = new Usuario("Judith", "judith@email.com");
		
		Leilao leilao1 = new Leilao("RTX 3080", 5000.00, judith, false);
		Calendar dataLeilao1 = Calendar.getInstance();
		dataLeilao1.add(Calendar.DAY_OF_MONTH, -2);
		leilao1.setDataAbertura(dataLeilao1);
		
		Leilao leilao2 = new Leilao("RTX 2080", 2000.00, judith, false);
		Calendar dataLeilao2 = Calendar.getInstance();
		dataLeilao2.add(Calendar.DAY_OF_MONTH, -20);
		leilao2.setDataAbertura(dataLeilao2);
		
		usuarioDao.salvar(judith);
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		
		List<Leilao> leiloes = leilaoDao.porPeriodo(inicio, fim);
		
		assertEquals(1, leiloes.size());
		assertEquals("RTX 3080", leiloes.get(0).getNome());
	}
	
	@Test
	public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {
		Calendar inicio = Calendar.getInstance();
		Calendar fim = Calendar.getInstance();
		inicio.add(Calendar.DAY_OF_MONTH, -10);
		
		Calendar dataLeilao1 = Calendar.getInstance();
		dataLeilao1.add(Calendar.DAY_OF_MONTH, -2);
		leilao.setDataAbertura(dataLeilao1);
		leilao.encerra();
		
		usuarioDao.salvar(leilao.getDono());
		leilaoDao.salvar(leilao);
		
		List<Leilao> leiloes = leilaoDao.porPeriodo(inicio, fim);
		
		assertEquals(0, leiloes.size());
	}
		
	@Test 
	public void deveRetornarLeiloesDisputadosNoPeriodo() {
        Usuario dono = new Usuario("Jose da Silva", "jose@dasilva.com.br");
        Usuario comprador1 = new Usuario("Joao", "joao@joao.com.br");
        Usuario comprador2 = new Usuario("Maria", "maria@maria.com.br");

        Leilao leilao1 = new LeilaoBuilder()
        		.comNome("Geladeira")
        		.comValor(1500.0)
        		.comDono(dono)
        		.diasAtras(2)
                .comLance(new Lance(Calendar.getInstance(),comprador1,2000.0))
                .encerrado()
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
        		.comNome("Nintendo Wii")
        		.comValor(2500.0)
        		.comDono(dono)
        		.diasAtras(6)
                .comLance(new Lance(Calendar.getInstance(),comprador1,3000.0))
                .comLance(new Lance(Calendar.getInstance(),comprador2,3500.0))
                .comLance(new Lance(Calendar.getInstance(),comprador1,4000.0))
                .constroi();

        Leilao leilao3 = new LeilaoBuilder()
        		.comNome("Xbox")
        		.comValor(3000.0)
        		.comDono(dono)
        		.diasAtras(12)
                .comLance(new Lance(Calendar.getInstance(),comprador1,4000.0))
                .usado()
                .constroi();

        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador1);
        usuarioDao.salvar(comprador2);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);
        leilaoDao.salvar(leilao3);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(1000.0, 3000.0);

        assertEquals(1, leiloes.size());
        assertEquals("Nintendo Wii", leiloes.get(0).getNome());
    }
	
	@Test
	public void deveRetornarLeiloesDeUsuario() {
        Usuario dono = new Usuario("Jose da Silva", "jose@dasilva.com.br");
        Usuario comprador1 = new Usuario("Joao", "joao@joao.com.br");

        Leilao leilao1 = new LeilaoBuilder()
        		.comNome("Geladeira")
        		.comValor(1500.0)
        		.comDono(dono)
        		.diasAtras(2)
                .comLance(new Lance(Calendar.getInstance(),comprador1,2000.0))
                .encerrado()
                .constroi();
        
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador1);
        leilaoDao.salvar(leilao1);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador1);

        assertEquals(1, leiloes.size());
        assertEquals("Geladeira", leiloes.get(0).getNome());
	}
	
    @Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .comLance(new Lance(Calendar.getInstance(), comprador, 100.0))
            .comLance(new Lance(Calendar.getInstance(), comprador, 200.0))
            .constroi();
        
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }
    
    @Test
    public void deveRetornarValorMedioDeLanceDoUsuario() {
    	Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        
        Leilao leilao1 = new LeilaoBuilder()
        	.comNome("RTX 2080")
        	.comValor(3000.00)
            .comDono(dono)
            .comLance(new Lance(Calendar.getInstance(), comprador, 100.0))
            .comLance(new Lance(Calendar.getInstance(), comprador, 200.0))
            .diasAtras(2)
            .constroi();
        
        Leilao leilao2 = new LeilaoBuilder()
        	.comNome("RTX 2080")
        	.comValor(2000.00)
            .comDono(dono)
            .comLance(new Lance(Calendar.getInstance(), comprador, 100.0))
            .diasAtras(2)
            .constroi();
        
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        double valor = leilaoDao.getValorInicialMedioDoUsuario(comprador);
        
        assertEquals(2500.00, valor, 0.00001);
    }
}
