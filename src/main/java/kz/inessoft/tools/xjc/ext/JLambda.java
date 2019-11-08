package kz.inessoft.tools.xjc.ext;

import com.sun.codemodel.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a single Java 8 lambda expression. It consists of 0-n parameters and
 * a body statement. For example in <code>(a, b) -&gt; a + b</code> "a" and "b"
 * are parameters and "a + b" is the body statement.
 *
 * @author Philip Helger
 * @since 2.7.10
 */
public class JLambda extends  JExpressionImpl
{
  private final List <JLambdaParam> m_aParams = new ArrayList <> ();
  private final JBlock m_aBodyStatement = new JBlock ();

  /**
   * Create an empty lambda without any parameter.
   */
  public JLambda ()
  {}

  /**
   * Add a parameter without a type name.
   *
   * @param sName
   *        The variable name to use. May not be <code>null</code>.
   * @return The created {@link JLambdaParam} object.
   */
  
  public JLambdaParam addParam ( final String sName)
  {
    final JLambdaParam aParam = new JLambdaParam ((JType) null, sName);
    m_aParams.add (aParam);
    return aParam;
  }

  /**
   * Add a parameter with a type name.
   *
   * @param aType
   *        The Type of the parameter. May be <code>null</code>.
   * @param sName
   *        The variable name to use. May not be <code>null</code>.
   * @return The created {@link JLambdaParam} object.
   */
  
  public JLambdaParam addParam (final JType aType,  final String sName)
  {
    final JLambdaParam aParam = new JLambdaParam (aType, sName);
    m_aParams.add (aParam);
    return aParam;
  }

  /**
   * @return An unmodifiable list with all parameters present. Never
   *         <code>null</code>.
   */
  
  public List <JLambdaParam> params ()
  {
    return Collections.unmodifiableList (m_aParams);
  }

  public int paramCount ()
  {
    return m_aParams.size ();
  }

  
  public JBlock body ()
  {
    return m_aBodyStatement;
  }

  @Override
  public void generate ( JFormatter f)
  {
    final int nParams = m_aParams.size ();
    if (nParams > 0)
    {
      final JLambdaParam aParam0 = m_aParams.get (0);
      for (int i = 1; i < nParams; ++i)
        if (m_aParams.get (i).hasType () != aParam0.hasType ())
          throw new IllegalStateException ("Lambda expression parameters must all have types or none may have a type!");
    }
    if (m_aBodyStatement.isEmpty ())
      throw new IllegalStateException ("Lambda expression is empty!");

    // Print parameters
    if (nParams == 0)
      f.p ("()");
    else
      if (nParams == 1 && !m_aParams.get (0).hasType ())
      {
        // Braces can be omitted for single parameters without a type
        m_aParams.get (0).declare (f);
      }
      else
      {
        f.p ('(');
        for (int i = 0; i < nParams; ++i)
        {
          if (i > 0)
            f.p (',');
          m_aParams.get (i).declare (f);
        }
        f.p (')');
      }
    f.p (" -> ");

    // Print body
    f.s (m_aBodyStatement);
  }
}
