package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;

	@Before
	public void antes() {
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
		session.beginTransaction();
	}
	
	@After
	public void depois() {
		session.getTransaction().rollback();
		session.close();
	}
	
	@Test
	public void deveEncontrarPeloNomeEEmail() {
		Usuario novoUsuario = new Usuario("Tobias", "tobias@email.com");
		
		usuarioDao.salvar(novoUsuario);
		
		Usuario usuario = usuarioDao.porNomeEEmail("Tobias", "tobias@email.com");
		
		assertEquals("Tobias", usuario.getNome());
		assertEquals("tobias@email.com", usuario.getEmail());
	}
	
	@Test
	public void deveRetornarNulloCasoNaoEncontreUsuario() {
		Session session = new CriadorDeSessao().getSession();
		
		UsuarioDao usuarioDao = new UsuarioDao(session);
		
		Usuario usuario = usuarioDao.porNomeEEmail("Cleidi", "cleidi@email.com");
		
		assertNull(usuario);
	}
	
	@Test
	public void deveDeletarUsuario() {
		Usuario usuario = new Usuario("Durval", "durval@email.com");
		
		usuarioDao.salvar(usuario);
		usuarioDao.deletar(usuario);
		
		session.flush();
		session.clear();
		
		Usuario deletado = usuarioDao.porNomeEEmail("Durval", "durval@email.com");
		
		assertNull(deletado);
	}
	
	@Test
	public void deveAlterarUsuario() {
		Usuario usuario = new Usuario("Durval", "durval@email.com");
		
		usuarioDao.salvar(usuario);
		
		usuario.setNome("Supremo Durval");
		
		usuarioDao.atualizar(usuario);
		
		//session.flush();
		//session.clear();
		
		Usuario antigo = usuarioDao.porNomeEEmail("Durval", "durval@email.com");
		Usuario alterado = usuarioDao.porNomeEEmail("Supremo Durval", "durval@email.com");
		
		assertNull(antigo);
		assertEquals("Supremo Durval", alterado.getNome());
	}
}
