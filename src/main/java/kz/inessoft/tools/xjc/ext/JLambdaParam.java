package kz.inessoft.tools.xjc.ext;

import com.sun.codemodel.*;


/**
 * This represent a single parameter to a Java 8 lambda expression.
 *
 * @author Philip Helger
 * @since 2.7.10
 */
public class JLambdaParam implements JDeclaration
{
  private final JType m_aType;
  private final String m_sName;

  public JLambdaParam (final JType aType, final String sName)
  {
    m_aType = aType;
    m_sName = sName;
  }

  
  public JType type ()
  {
    return m_aType;
  }

  public boolean hasType ()
  {
    return m_aType != null;
  }

  
  public String name ()
  {
    return m_sName;
  }

  public void declare (final JFormatter f)
  {
    if (m_aType != null)
      f.g (m_aType);
    f.id (m_sName);
  }

  public void generate (final JFormatter f)
  {
    f.id (m_sName);
  }
}
