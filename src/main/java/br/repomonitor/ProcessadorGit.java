package br.repomonitor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by rodrigovfs on 22/06/2017.
 *
 * C:\Users\rodrigovfs\IdeaProjects\ativa>git log --since=7.days --pretty=format:"%h - %an, %ad : %s"
 */
public class ProcessadorGit {

    public static final String COMANDO = "git log --since=7.days --pretty=format:\"%h - %an, %ad : %s\"";
    public static final String DIRETORIO = "C:\\Users\\rodrigovfs\\IdeaProjects\\ativa";
    public static final int PALAVRA_TAMANHO_MINIMO = 3;         //tamanho mínimo de palavra considerada(remove preposições e artigos pequenos)
    public static final int PALAVRA_FATOR_MULTIPLICACAO = 2;    //fator de multiplicação do valor de cada palavra encontrada
    public static final int VALOR_NOTA_INICIAL = 0;             //nota inicial do comentário
    public static final int VALOR_PALAVRA = 1;                  //valor de cada palavra encontrada (será multiplicado pelo fator)
    public static final int VALOR_ISSUE = 1;                    //valor adiconado à nota ao identificar uma issue no começo do comentário
    public static final int VALOR_NOTA_MAXIMA = 10;             //nota máxima a ser obtida em um comentário

    private String localPath, remotePath;
    private Repository localRepo;
    private Git git;

    public void init() throws IOException {
        localPath = DIRETORIO;
        //remotePath = "git@github.com:me/mytestrepo.git";
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }

    public void processar() throws IOException, GitAPIException {
        LogCommand log = git.log();
        Iterable<RevCommit> logs = log.call();

        Date dateFrom = new Date();
        for (RevCommit commit : logs) {
            Date commitTime = new Date(commit.getCommitTime() * 1000L);
            if (commitTime.before(dateFrom)) {
                int nota = pontuarCommit(commit.getShortMessage());
                System.out.print(commit.getAuthorIdent().getEmailAddress() + " - " + commit.getShortMessage()+ "("+nota+")"+"\n");
            } else {
                break;
            }
        }
    }

    private int pontuarCommit(String shortMessage) {
        int nota = VALOR_NOTA_INICIAL;
        int fator = PALAVRA_FATOR_MULTIPLICACAO;
        if (shortMessage != null) {
            nota = pontuarIssue(shortMessage);
            List<String> palavras = Arrays.asList(shortMessage.trim().split(" "));
            for (String palavra:palavras) {
                if (palavra.length() >= PALAVRA_TAMANHO_MINIMO) {
                    nota = nota + (fator * VALOR_PALAVRA);
                    if (nota > VALOR_NOTA_MAXIMA) {
                        nota = VALOR_NOTA_MAXIMA;
                        break;
                    }
                }
            }
        }
        return nota;
    }

    private int pontuarIssue(String shortMessage) {
        //Verifica se exite um comentário inciado com #NUMERO
        if(shortMessage.matches("[#][0-9].*")) {
            return VALOR_ISSUE;
        }
        return 0;
    }

    public static void main(String[] args) {
        ProcessadorGit processadorGit = new ProcessadorGit();
        try {
            processadorGit.init();
            processadorGit.processar();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
}
