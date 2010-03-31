function GenerateMapJet(TITLE,mapfile,matrix,MaxDisc,LinkNumber,ncols,nrows,xllcorner,yllcorner,cellsize,NODATA_value,v)
[l,c] = size(matrix);  
ld = length(MaxDisc);          
  for i=1:l
    for j=1:c
      if(matrix(i,j)) matrix_peak(i,j)=MaxDisc(matrix(i,j));
      else matrix_peak(i,j)=-2000;
      end
    end
  end

  for i=1:l
    for j=1:c
      if(matrix(i,j)) border(i,j)=1;
      else border(i,j)=-9.0;
      end
    end
  end
  
figure1 = figure('Color',[1 1 1],...
  'PaperUnits','centimeters',...
  'PaperPosition',[0.6345 6.345 20.3 15.23],...
  'PaperSize',[21.57 27.92]);
set(figure1,'PaperUnits','centimeters');


%set(figure1,'PaperSize',[10 15]);

XLmax= xllcorner+ cellsize*(ncols-1);
YLmax= yllcorner+ cellsize*(nrows-1);

[X,Y] = meshgrid([xllcorner:cellsize:XLmax],[YLmax:-cellsize:yllcorner]);

x=[xllcorner XLmax]
y=[yllcorner YLmax]

climsmin=min(v);
climsmax=max(v)
clims=[climsmin climsmax]
axes('Color','none')
imagesc(x,y,matrix_peak,clims)
colormap jet
colorbar('location','southoutside')
%contour(X,Y,border)
axis square

%tit=[TITLE(1);TITLE(2)]
title(TITLE,'FontSize',12,'FontName','Calibri');
%surf(X,Y,matrix_peak)
%colorbar
%colormap(map)

out =[mapfile '.bmp'];
saveas(figure1,out)
close(figure1) 

%out =[mapfile '.asc'];

%fid = fopen(out,'w');

      
%      fprintf(fid, 'NCOLS %d \n', ncols);
%      fprintf(fid, 'NROWS %d \n', nrows);      
%      fprintf(fid, 'XLLCORNER %f \n', xllcorner); 
%      fprintf(fid, 'YLLCORNER %f \n', yllcorner); 
%      fprintf(fid, 'CELLSIZE %f \n', cellsize); 
%      fprintf(fid, 'NODATA_value %f \n',  NODATA_value);       
    
    % *****************************************************************

%  for j=1:l
%       for i=1:c
%       fprintf(fid,'%g ',matrix_peak(j,i));
%       end
%       fprintf(fid, '\n');
%  end  

%    fclose(fid);

    