import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface SchoolContextType {
  currentSchoolId: number | null;
  currentSchoolName: string | null;
  selectSchool: (id: number, name: string) => void;
  clearSelection: () => void;
  isInitialized: boolean;
}

const SchoolContext = createContext<SchoolContextType | undefined>(undefined);

export const SchoolProvider = ({ children }: { children: ReactNode }) => {
  const [currentSchoolId, setCurrentSchoolId] = useState<number | null>(null);
  const [currentSchoolName, setCurrentSchoolName] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const savedId = localStorage.getItem('schoolMee_schoolId');
    const savedName = localStorage.getItem('schoolMee_schoolName');
    
    if (savedId && savedName) {
      setCurrentSchoolId(Number(savedId));
      setCurrentSchoolName(savedName);
    }
    setIsInitialized(true);
  }, []);

  const selectSchool = (id: number, name: string) => {
    setCurrentSchoolId(id);
    setCurrentSchoolName(name);
    localStorage.setItem('schoolMee_schoolId', id.toString());
    localStorage.setItem('schoolMee_schoolName', name);
  };

  const clearSelection = () => {
    setCurrentSchoolId(null);
    setCurrentSchoolName(null);
    localStorage.removeItem('schoolMee_schoolId');
    localStorage.removeItem('schoolMee_schoolName');
  };

  return (
    <SchoolContext.Provider value={{ currentSchoolId, currentSchoolName, selectSchool, clearSelection, isInitialized }}>
      {children}
    </SchoolContext.Provider>
  );
};

export const useSchool = () => {
  const context = useContext(SchoolContext);
  if (context === undefined) {
    throw new Error('useSchool must be used within a SchoolProvider');
  }
  return context;
};
