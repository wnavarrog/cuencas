function Gener_Asc(filename,RES,ncol,nrows,MINLON,MINLAT,Z)
        
        fid2=fopen(filename,'w');
           RES_M=RES/60;             
           fprintf(fid2,'ncols %d \n',ncol);
           fprintf(fid2,'nrows %d \n',nrows);           
           fprintf(fid2,'xllcorner %g \n',MINLON);
           fprintf(fid2,'yllcorner %g \n',MINLAT);
           fprintf(fid2,'cellsize %g \n',RES_M);           
           fprintf(fid2,'NODATA_value -9.00 \n');           
        
            for il=1:nrows
               for ic=1:ncol
                fprintf(fid2,'%0.2f ',Z(il,ic));
               end
               fprintf(fid2,'\n');            
            end
           fclose(fid2);