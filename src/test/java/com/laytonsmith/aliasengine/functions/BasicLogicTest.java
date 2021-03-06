/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Env;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static com.laytonsmith.testing.StaticTest.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class BasicLogicTest {
    
    Player fakePlayer;
    Server fakeServer;
    CArray commonArray;
    CInt arg1_1;
    CInt arg1_2;
    CInt arg2_1;
    CInt argn1_1;
    CInt argn2_1;
    CBoolean _true;
    CBoolean _false;
    Env env = new Env();
    
    public BasicLogicTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        commonArray = C.Array(C.Null(), C.Int(1), C.String("2"), C.Double(3.0));
        arg1_1 = C.Int(1);
        arg1_2 = C.Int(1);
        arg2_1 = C.Int(2);
        argn1_1 = C.Int(-1);
        argn2_1 = C.Int(-2);
        _true = C.Boolean(true);
        _false = C.Boolean(false);
        fakeServer = GetFakeServer();
        fakePlayer = GetOnlinePlayer(fakeServer);
        env.SetPlayer(fakePlayer);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDocs() {
        TestClassDocs(BasicLogic.docs(), BasicLogic.class);
    }
    
    @Test
    public void testEquals() throws CancelCommandException{
        BasicLogic.equals e = new BasicLogic.equals();

//             T   F   1   0  -1  '1' '0' '-1' N  {} 'CH'  '' 1.0
//        ---------------------------------------------------
//        T    T   F   T   F   T   T   T   T   F   F   T   F   T
//        F    -   T   F   T   F   F   F   F   T   T   F   T   F
//        1    -   -   T   F   F   T   F   F   F   F   F   F   T
//        0    -   -   -   T   F   F   T   F   F   F   F   F   F
//        -1   -   -   -   -   T   F   F   T   F   F   F   F   F
//        '1'  -   -   -   -   -   T   F   F   F   F   F   F   T
//        '0'  -   -   -   -   -   -   T   F   F   F   F   F   F
//        '-1' -   -   -   -   -   -   -   T   F   F   F   F   F
//        N    -   -   -   -   -   -   -   -   T   F   F   F   F
//        {}   -   -   -   -   -   -   -   -   -   T   F   F   F
//        'CH' -   -   -   -   -   -   -   -   -   -   T   F   F
//        ''   -   -   -   -   -   -   -   -   -   -   -   T   F
//        1.0  -   -   -   -   -   -   -   -   -   -   -   -   T

        _t("false", "false");
        _f("false", "1");
        _t("false", "0");
        //TODO: Finish
        
        _t("true", "true");
        _f("true", "false");
        _t("true", "1");
        _f("true", "0");
        _t("true", "-1");
        _t("true", "'1'");
        _t("true", "'0'");
        _t("true", "'-1'");
        _f("true", "null");
        _f("true", "array()");
        _t("true", "'CH'");
        _f("true", "''");
        _t("true", "1.0");
        
        
    }
    
    public void _t(String val1, String val2){
        try {
            assertEquals("true", SRun("equals(" + val1 + ", " + val2 + ")", null));
        } catch (ConfigCompileException ex) {
            fail(ex.getMessage());
        }
    }
    
    public void _f(String val1, String val2){
        try {
            assertEquals("false", SRun("equals(" + val1 + ", " + val2 + ")", null));
        } catch (ConfigCompileException ex) {
            fail(ex.getMessage());
        }
    }
    
    public void testEqualsIC() throws ConfigCompileException{
        SRun("if(equals_ic('hi', 'HI'), msg('pass'))", fakePlayer);
        SRun("if(equals_ic('hi', 'hi'), msg('pass'))", fakePlayer);
        SRun("if(not(equals_ic('what', 'hi')), msg('pass'))", fakePlayer);
        SRun("if(equals_ic(2, 2), msg('pass'))", fakePlayer);
        SRun("if(not(equals_ic(2, 'hi')), msg('pass'))", fakePlayer);
        verify(fakePlayer, times(5)).sendMessage("pass");
    }
    
    @Test public void testAnd1() throws CancelCommandException, ConfigCompileException{
        SRun("if(and(true, true, true), msg(pass))", fakePlayer);
        SRun("if(and(true, true, false), '', msg(pass))", fakePlayer);
        SRun("if(and(true, true), msg(pass))", fakePlayer);
        SRun("if(and(true, false), '', msg(pass))", fakePlayer);
        SRun("if(and(false, false), '', msg(pass))", fakePlayer);
        SRun("if(and(true), msg(pass))", fakePlayer);
        SRun("if(and(false), '', msg(pass))", fakePlayer);
        verify(fakePlayer, times(7)).sendMessage("pass");
    }
    
    /**
     * Tests lazy evaluation
     * @return
     * @throws ConfigCompileException 
     */
    @Test public void testAnd2() throws ConfigCompileException{
        SRun("and(false, msg(lol))", fakePlayer);
        verify(fakePlayer, times(0)).sendMessage("lol");
    }
    
    @Test public void testOr1() throws CancelCommandException, ConfigCompileException{
        SRun("if(or(true, true, true), msg(pass))", fakePlayer);
        SRun("if(or(true, true, false), msg(pass))", fakePlayer);
        SRun("if(or(true, true), msg(pass))", fakePlayer);
        SRun("if(or(true, false), msg(pass))", fakePlayer);
        SRun("if(or(false, false), '', msg(pass))", fakePlayer);
        SRun("if(or(true), msg(pass))", fakePlayer);
        SRun("if(or(false), '', msg(pass))", fakePlayer);
        verify(fakePlayer, times(7)).sendMessage("pass");
    }
    
    @Test public void testOr2() throws ConfigCompileException{
        SRun("or(true, msg(lol))", fakePlayer);
        verify(fakePlayer, times(0)).sendMessage("lol");
    }
    
    @Test public void testNot() throws CancelCommandException{
        BasicLogic.not a = new BasicLogic.not();
        assertCFalse(a.exec(0, null, env, _true));
        assertCTrue(a.exec(0, null, env, _false));
    }
    
    @Test public void testGt() throws CancelCommandException{
        BasicLogic.gt a = new BasicLogic.gt();
        assertCFalse(a.exec(0, null, env, arg1_1, arg1_2));
        assertCTrue(a.exec(0, null, env, arg2_1, arg1_1));
        assertCFalse(a.exec(0, null, env, arg1_1, arg2_1));
        assertCFalse(a.exec(0, null, env, argn1_1, arg1_1));
        assertCTrue(a.exec(0, null, env, arg1_1, argn1_1));
    }
    
    @Test public void testGte() throws CancelCommandException{
        BasicLogic.gte a = new BasicLogic.gte();
        assertCTrue(a.exec(0, null, env, arg1_1, arg1_2));
        assertCTrue(a.exec(0, null, env, arg2_1, arg1_1));
        assertCFalse(a.exec(0, null, env, arg1_1, arg2_1));
        assertCFalse(a.exec(0, null, env, argn1_1, arg1_1));
        assertCTrue(a.exec(0, null, env, arg1_1, argn1_1));
    }
    
    @Test public void testLt() throws CancelCommandException{
        BasicLogic.lt a = new BasicLogic.lt();
        assertCFalse(a.exec(0, null, env, arg1_1, arg1_2));
        assertCFalse(a.exec(0, null, env, arg2_1, arg1_1));
        assertCTrue(a.exec(0, null, env, arg1_1, arg2_1));
        assertCTrue(a.exec(0, null, env, argn1_1, arg1_1));
        assertCFalse(a.exec(0, null, env, arg1_1, argn1_1));
    }
    
    @Test public void testLte() throws CancelCommandException{
        BasicLogic.lte a = new BasicLogic.lte();
        assertCTrue(a.exec(0, null, env, arg1_1, arg1_2));
        assertCFalse(a.exec(0, null, env, arg2_1, arg1_1));
        assertCTrue(a.exec(0, null, env, arg1_1, arg2_1));
        assertCTrue(a.exec(0, null, env, argn1_1, arg1_1));
        assertCFalse(a.exec(0, null, env, arg1_1, argn1_1));
    }
    
    @Test public void testIf() throws ConfigCompileException{
        BasicLogic._if a = new BasicLogic._if();
        SRun("if(true, msg('correct'), msg('incorrect'))", fakePlayer);
        SRun("if(false, msg('incorrect'), msg('correct'))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("correct");
    }
    
    @Test public void testXor() throws ConfigCompileException{
        assertEquals("false", SRun("xor(false, false)", null));
        assertEquals("true", SRun("xor(false, true)", null));
        assertEquals("true", SRun("xor(true, false)", null));
        assertEquals("false", SRun("xor(true, true)", null));
    }
    @Test public void testNand() throws ConfigCompileException{
        assertEquals("true", SRun("nand(false, false)", null));
        assertEquals("true", SRun("nand(false, true)", null));
        assertEquals("true", SRun("nand(true, false)", null));
        assertEquals("false", SRun("nand(true, true)", null));
    }
    @Test public void testNor() throws ConfigCompileException{
        assertEquals("true", SRun("nor(false, false)", null));
        assertEquals("false", SRun("nor(false, true)", null));
        assertEquals("false", SRun("nor(true, false)", null));
        assertEquals("false", SRun("nor(true, true)", null));
    }
    @Test public void testXnor() throws ConfigCompileException{
        assertEquals("true", SRun("xnor(false, false)", null));
        assertEquals("false", SRun("xnor(false, true)", null));
        assertEquals("false", SRun("xnor(true, false)", null));
        assertEquals("true", SRun("xnor(true, true)", null));
    }
    
    @Test public void testBitAnd() throws ConfigCompileException{
        assertEquals("4", SRun("bit_and(4, 7)", null));
        assertEquals("5", SRun("bit_and(7, 5)", null));
        assertEquals("0", SRun("bit_and(1, 4)", null));
    }
    @Test public void testBitOr() throws ConfigCompileException{
        assertEquals("3", SRun("bit_or(1, 3)", null));
        assertEquals("6", SRun("bit_or(2, 4)", null));
    }
    @Test public void testBitNot() throws ConfigCompileException{
        assertEquals("-5", SRun("bit_not(4)", null));
    }
    @Test public void testLshift() throws ConfigCompileException{
        assertEquals("16", SRun("lshift(4, 2)", null));
    }
    @Test public void testRshift() throws ConfigCompileException{
        assertEquals("-3", SRun("rshift(-10, 2)", null));
        assertEquals("1", SRun("rshift(3, 1)", null));
    }
    @Test public void testUrshift() throws ConfigCompileException{
        assertEquals("2", SRun("urshift(10, 2)", null));
        assertEquals("4611686018427387901", SRun("urshift(-10, 2)", null));
    }
    
    @Test public void testIfelse() throws ConfigCompileException{
        assertEquals("3", SRun("ifelse("
                + "false, 1,"
                + "false, 2,"
                + "true, 3,"
                + "true, 4,"
                + "false, 5)", null));
        assertEquals("4", SRun("ifelse("
                + "false, 1,"
                + "false, 2,"
                + "false, 3,"
                + "add(2, 2))", null));
    }
    
    @Test public void testSwitch() throws ConfigCompileException{
        assertEquals("correct", SRun("switch(3,"
                + "1, wrong,"
                + "2, wrong,"
                + "3, correct,"
                + "4, wrong)", null));
        assertEquals("correct", SRun("switch(4,"
                + "1, wrong,"
                + "2, wrong,"
                + "3, wrong,"
                + "correct)", null));
    }
    
    @Test public void testSequals() throws ConfigCompileException{
        assertEquals("true", SRun("sequals(1, 1)", null));
        assertEquals("false", SRun("sequals(1, '1')", null));
        assertEquals("false", SRun("sequals(1, '2')", null));
    }
}
